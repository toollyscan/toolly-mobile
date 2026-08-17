import Foundation
import ToollySharedUI

/// First-party, AES-256-GCM-encrypted implementation of the `AppleDocumentVaultSession` boundary
/// declared in `AppleDocumentVaultBridge.kt` (TLY-014 Phase 3, ADR-0012, Tier-2 "iOS vault
/// encryption"). Every manifest and page asset is encrypted before it ever touches disk, via
/// `ToollyVaultCipher` (`vault/crypto/ToollyVaultCrypto.swift`) -- envelope AES-256-GCM with a
/// Keychain-held wrapping key, mirroring `EncryptedDocumentRepository`'s Android Keystore design.
/// Plaintext exists only in the bounded capture staging area owned by `resolveTemporaryAsset` and
/// in bounded decode/encode memory here; nothing plaintext is ever written under
/// `documentsDirectory`.
///
/// ## Deliberately still open (not silently missing)
/// - **No staged/commit-marker transaction discipline yet.** `EncryptedDocumentRepository` writes
///   into a `.staging` transaction directory and only promotes it via an atomic rename once every
///   asset and the manifest are written and verified, so an interrupted multi-page save can never
///   leave a half-written document readable. This class still writes pages directly into the
///   final document directory one at a time (same structural shape as Phase 2); an interrupted
///   save can leave a partial directory with no manifest, which simply fails to list/open on
///   reopen (fails closed, not silently corrupt) but isn't cleaned up automatically. Bringing over
///   Android's full staging-transaction protocol is a scoped follow-up, not this change.
/// - **No streaming/chunked asset encryption.** `ToollyVaultCipher` seals each page as a single
///   AES-GCM operation (see its own doc comment for why that's safe at these bounded sizes), not
///   Android's per-chunk streaming design -- fine for JPEG pages, would need revisiting only if
///   much larger assets are ever supported.
/// - **No decrypt-to-bitmap consumer wired up yet.** Nothing on iOS currently reads page pixel
///   data back out of the vault (no document viewer exists there yet), so this class doesn't
///   expose a `loadAssetData` equivalent to Android's `loadAssetBitmap` -- `decryptAsset` is ready
///   on `ToollyVaultCipher` for whichever screen needs it first.
///
/// ## Not yet verified against a real Xcode build
/// Written without access to Xcode or a macOS toolchain, same caveat as every other first-party
/// Swift file in this app. The Kotlin side of this boundary (`AppleDocumentVaultBridge.kt`) is
/// compiler-verified via `:shared-ui:compileTestKotlinIosSimulatorArm64`; this file and
/// `ToollyVaultCrypto.swift` need a real Xcode build and a physical-device/simulator pass to
/// confirm CryptoKit/Keychain usage here actually behaves as written -- no Swift unit test exists
/// yet for either (same gap noted for the rest of this vault slice; would need a Tests target
/// added to the Xcode project, not attempted here without Xcode to verify it against).
final class AppleDocumentVaultSessionImpl: NSObject, AppleDocumentVaultSession {
    private let documentsDirectory: URL
    private let resolveTemporaryAsset: (String) -> URL?
    private let cipher: ToollyVaultCipher
    private let vaultScopeId: String

    /// `resolveTemporaryAsset` resolves a temporary asset id staged by capture back to its file --
    /// pass `captureSession.fileURL(forTemporaryAssetId:)` when wiring this up in `ToollyApp.swift`,
    /// matching `AppleCaptureSessionImpl`'s own documented "API whoever builds the vault next will
    /// need".
    init(resolveTemporaryAsset: @escaping (String) -> URL?) {
        self.resolveTemporaryAsset = resolveTemporaryAsset
        let appSupport = FileManager.default.urls(
            for: .applicationSupportDirectory,
            in: .userDomainMask
        )[0]
        let root = appSupport.appendingPathComponent("toolly-vault-v1", isDirectory: true)
        let resolvedDocumentsDirectory = root.appendingPathComponent("documents", isDirectory: true)

        // Must exist before `ToollyVaultScope.loadOrCreate` below can persist a fresh scope file
        // into `root` -- and must happen here, before `self`/`super.init()` are available to do
        // it any other way (every stored property, `vaultScopeId` included, has to be set before
        // `super.init()` runs, so there's no later point to create this first).
        try? FileManager.default.createDirectory(
            at: resolvedDocumentsDirectory,
            withIntermediateDirectories: true
        )

        self.documentsDirectory = resolvedDocumentsDirectory
        self.cipher = ToollyVaultCipher(wrappingKey: ToollyVaultWrappingKey(account: "primary"))
        // Non-secret, per-install domain-separation value mixed into every AAD below -- see
        // `ToollyVaultScope`'s doc comment. Falls back to a fresh, non-persisted scope only if the
        // directory just created above is somehow still unwritable; an existing encrypted document
        // under a different scope simply fails to authenticate afterwards rather than silently
        // misreading, matching how a lost/rotated wrapping key already fails closed.
        self.vaultScopeId = (try? ToollyVaultScope.loadOrCreate(
            at: root.appendingPathComponent("vault.scope")
        )) ?? UUID().uuidString.lowercased()
        super.init()
        // Excluded from iCloud/iTunes backup -- local vault data should never leave the device via
        // backup (matches ADR-0012's storage boundary), independent of the fact it's now encrypted.
        var excludable = resolvedDocumentsDirectory
        var resourceValues = URLResourceValues()
        resourceValues.isExcludedFromBackup = true
        try? excludable.setResourceValues(resourceValues)
    }

    func listDocuments(callback: AppleDocumentListCallback) {
        guard let entries = try? FileManager.default.contentsOfDirectory(
            at: documentsDirectory,
            includingPropertiesForKeys: nil
        ) else {
            callback.onSuccess(documents: [])
            return
        }
        do {
            let summaries = try entries.compactMap { directory -> AppleDocumentSummaryDto? in
                guard manifestExists(documentId: directory.lastPathComponent) else { return nil }
                return try readManifest(documentId: directory.lastPathComponent).toSummaryDto()
            }
            callback.onSuccess(documents: summaries)
        } catch {
            callback.onFailure(errorCode: error.toollyWireErrorCode)
        }
    }

    func getDocument(documentId: String, callback: AppleDocumentCallback) {
        guard manifestExists(documentId: documentId) else {
            callback.onFailure(errorCode: "unavailable")
            return
        }
        do {
            let manifest = try readManifest(documentId: documentId)
            deliver(manifest, callback: callback)
        } catch {
            callback.onFailure(errorCode: error.toollyWireErrorCode)
        }
    }

    func saveCapturedDocument(
        operationId: String,
        documentId: String,
        createdAtEpochMillis: Int64,
        pages: [AppleCapturedPageInput],
        callback: AppleDocumentCallback
    ) {
        let directory = documentDirectory(for: documentId)
        do {
            try FileManager.default.createDirectory(
                at: directory,
                withIntermediateDirectories: true
            )
            var pageEntries: [PageEntry] = []
            for page in pages {
                guard let sourceURL = resolveTemporaryAsset(page.temporaryAssetId) else {
                    try? FileManager.default.removeItem(at: directory)
                    callback.onFailure(errorCode: "unavailable")
                    return
                }
                let plaintext = try requireBoundedJpeg(at: sourceURL)
                let ciphertext = try cipher.encryptAsset(
                    plaintext: plaintext,
                    vaultScopeId: vaultScopeId,
                    assetId: page.assetId,
                    assetKind: .sourceImage
                )
                let destinationURL = directory.appendingPathComponent(assetFileName(page.assetId))
                try ciphertext.write(to: destinationURL, options: .atomic)
                pageEntries.append(
                    PageEntry(
                        pageId: page.pageId,
                        assetId: page.assetId,
                        ordinal: Int(page.ordinal),
                        // NOT `page.widthPixels?.int32Value.map(Int.init)`: `.map` there falls
                        // inside the `?.` chain's scope, so Swift resolves it as a (nonexistent)
                        // member of the unwrapped `Int32` itself rather than of the optional --
                        // a real compile error only surfaced once this file was first actually
                        // registered in the Xcode project's build phase (see #102).
                        widthPixels: page.widthPixels.map { Int($0.int32Value) },
                        heightPixels: page.heightPixels.map { Int($0.int32Value) }
                    )
                )
            }
            let manifest = Manifest(
                documentId: documentId,
                createdAtEpochMillis: createdAtEpochMillis,
                updatedAtEpochMillis: createdAtEpochMillis,
                lifecycle: "ACTIVE",
                displayName: nil,
                category: nil,
                pages: pageEntries.sorted { $0.ordinal < $1.ordinal }
            )
            try writeManifest(manifest)
            deliver(manifest, callback: callback)
        } catch {
            try? FileManager.default.removeItem(at: directory)
            callback.onFailure(errorCode: error.toollyWireErrorCode)
        }
    }

    func renameDocument(
        documentId: String,
        displayName: String?,
        updatedAtEpochMillis: Int64,
        callback: AppleDocumentCallback
    ) {
        updateManifest(
            documentId: documentId,
            updatedAtEpochMillis: updatedAtEpochMillis,
            callback: callback
        ) { $0.displayName = displayName }
    }

    func tagDocument(
        documentId: String,
        category: String?,
        updatedAtEpochMillis: Int64,
        callback: AppleDocumentCallback
    ) {
        updateManifest(
            documentId: documentId,
            updatedAtEpochMillis: updatedAtEpochMillis,
            callback: callback
        ) { $0.category = category }
    }

    // MARK: - Manifest read/write (encrypted)

    private func documentDirectory(for documentId: String) -> URL {
        documentsDirectory.appendingPathComponent(documentId, isDirectory: true)
    }

    private func manifestURL(for documentId: String) -> URL {
        documentDirectory(for: documentId).appendingPathComponent("manifest.tlym")
    }

    private func manifestExists(documentId: String) -> Bool {
        FileManager.default.fileExists(atPath: manifestURL(for: documentId).path)
    }

    /// Reads and decrypts a document's manifest. Throws (never returns a partial/best-effort
    /// result) on any tamper, corruption, or missing-key condition -- callers map the thrown
    /// error to a wire error code via `Error.toollyWireErrorCode` rather than silently treating a
    /// document as absent, matching `EncryptedDocumentRepository.readDocument`'s fail-closed
    /// behavior on the Android side.
    private func readManifest(documentId: String) throws -> Manifest {
        let envelope = try Data(contentsOf: manifestURL(for: documentId))
        guard !envelope.isEmpty, envelope.count <= Self.maxManifestEnvelopeBytes else {
            throw ToollyVaultCryptoError.invalidEnvelope
        }
        let plaintext = try cipher.decryptMetadata(
            envelope: envelope,
            vaultScopeId: vaultScopeId,
            recordId: documentId,
            recordKind: .document
        )
        let manifest = try JSONDecoder().decode(Manifest.self, from: plaintext)
        guard manifest.documentId == documentId else { throw ToollyVaultCryptoError.invalidEnvelope }
        return manifest
    }

    /// Write-then-replace rather than an in-place overwrite, so an interrupted write can only
    /// leave a stray `.tmp` file behind, never a half-written manifest.
    private func writeManifest(_ manifest: Manifest) throws {
        let plaintext = try JSONEncoder().encode(manifest)
        let ciphertext = try cipher.encryptMetadata(
            plaintext: plaintext,
            vaultScopeId: vaultScopeId,
            recordId: manifest.documentId,
            recordKind: .document
        )
        let destination = manifestURL(for: manifest.documentId)
        let pending = destination.appendingPathExtension("tmp")
        try ciphertext.write(to: pending, options: .atomic)
        _ = try FileManager.default.replaceItemAt(destination, withItemAt: pending)
    }

    private func updateManifest(
        documentId: String,
        updatedAtEpochMillis: Int64,
        callback: AppleDocumentCallback,
        transform: (inout Manifest) -> Void
    ) {
        guard manifestExists(documentId: documentId) else {
            callback.onFailure(errorCode: "unavailable")
            return
        }
        do {
            var manifest = try readManifest(documentId: documentId)
            transform(&manifest)
            manifest.updatedAtEpochMillis = updatedAtEpochMillis
            try writeManifest(manifest)
            deliver(manifest, callback: callback)
        } catch {
            callback.onFailure(errorCode: error.toollyWireErrorCode)
        }
    }

    private func deliver(_ manifest: Manifest, callback: AppleDocumentCallback) {
        callback.onSuccess(
            documentId: manifest.documentId,
            pageCount: Int32(manifest.pages.count),
            createdAtEpochMillis: manifest.createdAtEpochMillis,
            updatedAtEpochMillis: manifest.updatedAtEpochMillis,
            lifecycle: manifest.lifecycle,
            displayName: manifest.displayName,
            category: manifest.category,
            pages: manifest.pages.map { $0.toDto() }
        )
    }

    // MARK: - Asset helpers

    private func assetFileName(_ assetId: String) -> String { "\(assetId).tlya" }

    /// Reads a captured page's staged JPEG and validates it is complete and within the vault's
    /// bound before it is ever handed to the cipher -- mirrors
    /// `EncryptedDocumentRepository.requireCompleteJpeg`'s SOI/EOI marker check, so a
    /// truncated/corrupt capture output fails here with a clear, retryable outcome rather than
    /// being encrypted and only discovered broken on next read.
    private func requireBoundedJpeg(at url: URL) throws -> Data {
        let data = try Data(contentsOf: url)
        guard data.count >= 4, data.count <= Self.maxPageBytes else {
            throw ToollyVaultCryptoError.platformFailure
        }
        let soi: [UInt8] = [0xFF, 0xD8]
        let eoi: [UInt8] = [0xFF, 0xD9]
        guard data.prefix(2).elementsEqual(soi), data.suffix(2).elementsEqual(eoi) else {
            throw ToollyVaultCryptoError.platformFailure
        }
        return data
    }

    private static let maxManifestEnvelopeBytes = 300 * 1024
    private static let maxPageBytes = 25 * 1024 * 1024
}

private struct Manifest: Codable {
    let documentId: String
    let createdAtEpochMillis: Int64
    var updatedAtEpochMillis: Int64
    var lifecycle: String
    var displayName: String?
    var category: String?
    var pages: [PageEntry]

    func toSummaryDto() -> AppleDocumentSummaryDto {
        AppleDocumentSummaryDto(
            documentId: documentId,
            pageCount: Int32(pages.count),
            createdAtEpochMillis: createdAtEpochMillis,
            updatedAtEpochMillis: updatedAtEpochMillis,
            lifecycle: lifecycle,
            displayName: displayName,
            category: category
        )
    }
}

private struct PageEntry: Codable {
    let pageId: String
    let assetId: String
    let ordinal: Int
    let widthPixels: Int?
    let heightPixels: Int?

    func toDto() -> AppleDocumentPageDto {
        AppleDocumentPageDto(
            pageId: pageId,
            assetId: assetId,
            ordinal: Int32(ordinal),
            widthPixels: widthPixels.map { KotlinInt(int: Int32($0)) },
            heightPixels: heightPixels.map { KotlinInt(int: Int32($0)) }
        )
    }
}

/// Maps any thrown error to `AppleDocumentVaultSession`'s lowercase-snake-case wire codes -- keep
/// in sync with `AppleDocumentVaultBridge.kt`'s `toToollyError()`. Anything not recognized as a
/// `ToollyVaultCryptoError` (a plain Foundation I/O error, for instance) degrades to `"retryable"`
/// rather than crashing or leaking an unmapped code across the Kotlin boundary.
private extension Error {
    var toollyWireErrorCode: String {
        (self as? ToollyVaultCryptoError)?.wireErrorCode ?? "retryable"
    }
}
