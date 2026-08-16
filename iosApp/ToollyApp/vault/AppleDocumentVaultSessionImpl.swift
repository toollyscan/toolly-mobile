import Foundation
import ToollySharedUI

/// First-party, unencrypted local implementation of the `AppleDocumentVaultSession` boundary
/// declared in `AppleDocumentVaultBridge.kt` (TLY-014 Phase 2, #82). Stores each document as a
/// plain JSON manifest plus copied JPEG page files under Application Support -- deliberately NO
/// cryptography yet, matching Phase 2's "prove the port boundary and plumbing compile end to end
/// first" scope (see #82's phased plan). This is not production-approved; Phase 3 replaces the
/// storage internals here with the real CryptoKit/Keychain implementation ADR-0012 requires,
/// behind this exact same Kotlin-facing interface, so nothing above this class needs to change
/// when that lands.
///
/// ## Not yet verified against a real Xcode build
/// Written without access to Xcode or a macOS toolchain, same caveat as
/// `AppleAccountAuthenticatorSessionImpl.swift`. The Kotlin side of this boundary
/// (`AppleDocumentVaultBridge.kt`) is compiler-verified via `:shared-ui:compileTestKotlinIosSimulatorArm64`;
/// this file needs a real Xcode build to confirm the generated Objective-C selectors below match
/// what Kotlin/Native actually emits (the boxed-optional-Int unboxing in `saveCapturedDocument`
/// is the single most likely spot to need a signature fix). Deliberately NOT wired into
/// `ToollyApp.swift` or added to the Xcode target's Sources build phase yet -- see #82 for the
/// remaining integration step.
final class AppleDocumentVaultSessionImpl: NSObject, AppleDocumentVaultSession {
    private let documentsDirectory: URL
    private let resolveTemporaryAsset: (String) -> URL?

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
        self.documentsDirectory = appSupport
            .appendingPathComponent("toolly-vault-unencrypted", isDirectory: true)
            .appendingPathComponent("documents", isDirectory: true)
        super.init()
        try? FileManager.default.createDirectory(
            at: documentsDirectory,
            withIntermediateDirectories: true
        )
        // Excluded from iCloud/iTunes backup -- local vault data, even this pre-crypto slice,
        // should never leave the device via backup (matches ADR-0012's storage boundary).
        var excludable = documentsDirectory
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
        let summaries = entries.compactMap { directory in
            readManifest(documentId: directory.lastPathComponent)?.toSummaryDto()
        }
        callback.onSuccess(documents: summaries)
    }

    func getDocument(documentId: String, callback: AppleDocumentCallback) {
        guard let manifest = readManifest(documentId: documentId) else {
            callback.onFailure(errorCode: "unavailable")
            return
        }
        deliver(manifest, callback: callback)
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
                let destinationURL = directory.appendingPathComponent("\(page.pageId).jpg")
                try FileManager.default.copyItem(at: sourceURL, to: destinationURL)
                pageEntries.append(
                    PageEntry(
                        pageId: page.pageId,
                        assetId: page.assetId,
                        ordinal: Int(page.ordinal),
                        widthPixels: page.widthPixels?.int32Value.map(Int.init),
                        heightPixels: page.heightPixels?.int32Value.map(Int.init)
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
            callback.onFailure(errorCode: "retryable")
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

    // MARK: - Manifest read/write

    private func documentDirectory(for documentId: String) -> URL {
        documentsDirectory.appendingPathComponent(documentId, isDirectory: true)
    }

    private func manifestURL(for documentId: String) -> URL {
        documentDirectory(for: documentId).appendingPathComponent("manifest.json")
    }

    private func readManifest(documentId: String) -> Manifest? {
        guard let data = try? Data(contentsOf: manifestURL(for: documentId)) else { return nil }
        return try? JSONDecoder().decode(Manifest.self, from: data)
    }

    /// Write-then-replace rather than an in-place overwrite, so an interrupted write can only
    /// leave a stray `.tmp` file behind, never a half-written manifest. Not the full staged/
    /// authenticated commit protocol ADR-0012 requires for the real vault -- this is Phase 2's
    /// "no cryptography yet" slice, and this is best-effort hygiene, not that guarantee.
    private func writeManifest(_ manifest: Manifest) throws {
        let data = try JSONEncoder().encode(manifest)
        let destination = manifestURL(for: manifest.documentId)
        let pending = destination.appendingPathExtension("tmp")
        try data.write(to: pending, options: .atomic)
        _ = try FileManager.default.replaceItemAt(destination, withItemAt: pending)
    }

    private func updateManifest(
        documentId: String,
        updatedAtEpochMillis: Int64,
        callback: AppleDocumentCallback,
        transform: (inout Manifest) -> Void
    ) {
        guard var manifest = readManifest(documentId: documentId) else {
            callback.onFailure(errorCode: "unavailable")
            return
        }
        transform(&manifest)
        manifest.updatedAtEpochMillis = updatedAtEpochMillis
        do {
            try writeManifest(manifest)
            deliver(manifest, callback: callback)
        } catch {
            callback.onFailure(errorCode: "retryable")
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
