import CryptoKit
import Foundation
import Security

/// Errors surfaced by Toolly's iOS vault cryptography. Mapped to `AppleDocumentVaultSession`'s
/// lowercase-snake-case error codes at the call site in `AppleDocumentVaultSessionImpl` -- keep
/// that mapping in sync with `AppleDocumentVaultBridge.kt`'s `toToollyError()`.
enum ToollyVaultCryptoError: Error, Equatable {
    case keyUnavailable
    case invalidEnvelope
    case authenticationFailed
    case platformFailure

    /// The lowercase-snake-case code `AppleDocumentVaultSessionImpl` reports across the Kotlin
    /// boundary for this failure -- keep in sync with `AppleDocumentVaultBridge.kt`'s
    /// `toToollyError()`. A key that vanished (e.g. Keychain wiped by an OS restore) is
    /// `"unauthorized"`, not `"corrupt"`: the ciphertext itself may be perfectly intact.
    var wireErrorCode: String {
        switch self {
        case .keyUnavailable: return "unauthorized"
        case .invalidEnvelope, .authenticationFailed: return "corrupt"
        case .platformFailure: return "retryable"
        }
    }
}

/// Holds Toolly's iOS vault wrapping key in the Keychain -- the platform-only root of trust
/// ADR-0012 requires, mirroring `AndroidAssetCipher`/`AndroidMetadataCipher`'s "Android Keystore
/// wraps a random per-record data key" design (`spike-capture/.../vault/crypto/`). Every record
/// still gets its own fresh random data key; this key only ever wraps those, never plaintext
/// document content directly.
///
/// `kSecAttrAccessibleWhenUnlockedThisDeviceOnly` is the closest Keychain equivalent to
/// `AndroidKeyStore`'s device-bound, non-exportable guarantee for a *symmetric* key: it keeps the
/// key out of iCloud Keychain sync and inaccessible before the device's first unlock after boot.
/// (The Secure Enclave itself only stores asymmetric P-256 keys, not AES keys, so it isn't a
/// direct substitute here -- an EC-wrapped scheme is a possible future hardening step, not this
/// phase's scope.)
final class ToollyVaultWrappingKey {
    private let account: String
    private let service: String

    init(account: String, service: String = "com.toollyscan.app.vault.wrap.v1") {
        self.account = account
        self.service = service
    }

    /// Used by encryption -- creates the key on first use if the Keychain has none yet.
    func getOrCreate() throws -> SymmetricKey {
        if let existing = try read() { return existing }
        let generated = SymmetricKey(size: .bits256)
        try store(generated)
        // Read back rather than trusting the just-generated value still held in memory, so a
        // silent Keychain write failure surfaces immediately instead of only on first decrypt.
        guard let verified = try read() else { throw ToollyVaultCryptoError.platformFailure }
        return verified
    }

    /// Used by decryption -- a missing key must fail closed as `.keyUnavailable`, never silently
    /// mint a new one that can't possibly open existing ciphertext.
    func getExisting() throws -> SymmetricKey {
        guard let existing = try read() else { throw ToollyVaultCryptoError.keyUnavailable }
        return existing
    }

    private func read() throws -> SymmetricKey? {
        // No kSecUseDataProtectionKeychain here: that flag opts into the newer, stricter
        // per-app "data protection keychain" namespace, which requires a keychain-access-groups
        // entitlement this app doesn't declare -- confirmed by CI (every test failed with
        // keyUnavailable, including a plain read of a never-used account, meaning even the
        // existence check itself couldn't reach the keychain). The traditional keychain works
        // fine for a simulator-only app with no such entitlement, and matches deleteForTesting's
        // query below, which never had this flag either -- store/read now agree with it.
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account,
            kSecReturnData as String: true,
        ]
        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)
        switch status {
        case errSecSuccess:
            guard let data = result as? Data, data.count == 32 else {
                throw ToollyVaultCryptoError.platformFailure
            }
            return SymmetricKey(data: data)
        case errSecItemNotFound:
            return nil
        default:
            throw ToollyVaultCryptoError.keyUnavailable
        }
    }

    private func store(_ key: SymmetricKey) throws {
        let keyData = key.withUnsafeBytes { Data($0) }
        let attributes: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account,
            kSecValueData as String: keyData,
            kSecAttrAccessible as String: kSecAttrAccessibleWhenUnlockedThisDeviceOnly,
        ]
        let status = SecItemAdd(attributes as CFDictionary, nil)
        guard status == errSecSuccess else { throw ToollyVaultCryptoError.platformFailure }
    }

    /// Test-only: removes the wrapping key so tests can exercise the "key unavailable" path,
    /// mirroring `AndroidAssetCipher.deleteWrappingKeyForTesting()`.
    func deleteForTesting() {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account,
        ]
        SecItemDelete(query as CFDictionary)
    }
}

/// Domain-separates every AAD-bound encryption operation this vault performs -- mirrors
/// `AssetAadPurpose`/`AadPurpose` on the Android side. Ciphertext sealed for one purpose can never
/// be opened as if it were another, even with the same key and record id.
private enum ToollyAadPurpose: UInt32 {
    case wrappedKey = 1
    case content = 2
}

/// The record kind an encrypted metadata payload belongs to -- bound into its AAD, mirrors
/// `RecordKind.DOCUMENT`.
enum ToollyVaultRecordKind: UInt32 {
    case document = 1
}

/// The kind of asset an encrypted file holds -- bound into its AAD, mirrors
/// `AssetObjectKind.SOURCE_IMAGE`.
enum ToollyVaultAssetKind: UInt32 {
    case sourceImage = 1
}

private let toollyVaultAadVersion: UInt32 = 1
let toollyVaultSchemaVersion: UInt32 = 1

/// Builds the authenticated-but-not-encrypted associated data every envelope in this vault binds
/// its ciphertext to -- a length-prefixed encoding so no field can be confused with a neighbour,
/// matching `MetadataAssociatedData.encode`/`AssetAssociatedData.encode`'s discipline on the
/// Android side. The two platforms do not need byte-identical AAD; each only ever decrypts its
/// own ciphertext.
private func toollyVaultAad(
    purpose: ToollyAadPurpose,
    vaultScopeId: String,
    recordId: String,
    kind: UInt32,
    schemaVersion: UInt32
) -> Data {
    var out = Data()
    func appendU32(_ value: UInt32) {
        withUnsafeBytes(of: value.bigEndian) { out.append(contentsOf: $0) }
    }
    func appendField(_ value: String) {
        let encoded = Data(value.utf8)
        appendU32(UInt32(encoded.count))
        out.append(encoded)
    }
    appendU32(toollyVaultAadVersion)
    appendU32(purpose.rawValue)
    appendField(vaultScopeId)
    appendField(recordId)
    appendU32(kind)
    appendU32(schemaVersion)
    return out
}

/// Binary envelope written to disk for both metadata and (whole-file) asset ciphertext:
/// `MAGIC | VERSION | wrappedKeyLength | wrappedKey | contentLength | content`, where both fields
/// are CryptoKit `AES.GCM.SealedBox.combined` blobs (nonce + ciphertext + tag already bundled).
/// Letting CryptoKit own nonce generation entirely removes an entire class of nonce-reuse bugs the
/// Android side has to guard against manually (see `AndroidAssetCipher`'s comment on Keystore
/// rejecting caller-supplied IVs for its wrapping key).
private enum ToollyVaultEnvelopeCodec {
    static let magic: UInt32 = 0x544C_5956 // "TLYV"
    static let version: UInt32 = 1
    static let maxFieldBytes = 26 * 1024 * 1024 // headroom above the 25 MiB asset cap below

    static func encode(wrappedKey: Data, content: Data) -> Data {
        var out = Data()
        func appendU32(_ value: UInt32) {
            withUnsafeBytes(of: value.bigEndian) { out.append(contentsOf: $0) }
        }
        appendU32(magic)
        appendU32(version)
        appendU32(UInt32(wrappedKey.count))
        out.append(wrappedKey)
        appendU32(UInt32(content.count))
        out.append(content)
        return out
    }

    static func decode(_ data: Data) throws -> (wrappedKey: Data, content: Data) {
        var offset = data.startIndex

        // Read byte-by-byte rather than via a typed load from the buffer -- `data` may be sliced
        // at an arbitrary, non-4-byte-aligned offset, and a typed load over misaligned memory is
        // undefined behavior without `loadUnaligned` (Swift 5.7+/iOS 16+, not assumed here).
        func readU32() throws -> UInt32 {
            guard data.distance(from: offset, to: data.endIndex) >= 4 else {
                throw ToollyVaultCryptoError.invalidEnvelope
            }
            var value: UInt32 = 0
            for _ in 0..<4 {
                value = (value << 8) | UInt32(data[offset])
                offset = data.index(after: offset)
            }
            return value
        }

        func readField() throws -> Data {
            let length = try readU32()
            guard length <= maxFieldBytes,
                  data.distance(from: offset, to: data.endIndex) >= Int(length) else {
                throw ToollyVaultCryptoError.invalidEnvelope
            }
            let field = data[offset..<data.index(offset, offsetBy: Int(length))]
            offset = data.index(offset, offsetBy: Int(length))
            return Data(field)
        }

        guard try readU32() == magic else { throw ToollyVaultCryptoError.invalidEnvelope }
        guard try readU32() == version else { throw ToollyVaultCryptoError.invalidEnvelope }
        let wrappedKey = try readField()
        let content = try readField()
        guard offset == data.endIndex else { throw ToollyVaultCryptoError.invalidEnvelope }
        return (wrappedKey, content)
    }
}

/// Real AES-256-GCM envelope encryption for the iOS vault (ADR-0012, TLY-014 Phase 3): every
/// record gets a fresh random 256-bit data key; the Keychain-held wrapping key
/// (`ToollyVaultWrappingKey`) encrypts that data key, never the plaintext directly.
///
/// Content is encrypted in one shot rather than Android's chunked-streaming design
/// (`AndroidAssetCipher`) -- a deliberate simplification for this phase, not an oversight: iOS
/// vault payloads are bounded the same way Android's are (300 KiB metadata / 25 MiB asset caps,
/// enforced below), comfortably inside AES-GCM's safe single-key/single-nonce usage limits, and
/// nothing on iOS decodes a page incrementally today. Streaming/chunked asset encryption is a
/// reasonable future hardening step if very large pages are ever supported, not this phase's scope.
final class ToollyVaultCipher {
    private let wrappingKey: ToollyVaultWrappingKey

    init(wrappingKey: ToollyVaultWrappingKey) {
        self.wrappingKey = wrappingKey
    }

    func encryptMetadata(
        plaintext: Data,
        vaultScopeId: String,
        recordId: String,
        recordKind: ToollyVaultRecordKind
    ) throws -> Data {
        try encrypt(
            plaintext: plaintext,
            vaultScopeId: vaultScopeId,
            recordId: recordId,
            kind: recordKind.rawValue,
            maxBytes: 300 * 1024
        )
    }

    func decryptMetadata(
        envelope: Data,
        vaultScopeId: String,
        recordId: String,
        recordKind: ToollyVaultRecordKind
    ) throws -> Data {
        try decrypt(
            envelope: envelope,
            vaultScopeId: vaultScopeId,
            recordId: recordId,
            kind: recordKind.rawValue
        )
    }

    func encryptAsset(
        plaintext: Data,
        vaultScopeId: String,
        assetId: String,
        assetKind: ToollyVaultAssetKind
    ) throws -> Data {
        try encrypt(
            plaintext: plaintext,
            vaultScopeId: vaultScopeId,
            recordId: assetId,
            kind: assetKind.rawValue,
            maxBytes: 25 * 1024 * 1024
        )
    }

    func decryptAsset(
        envelope: Data,
        vaultScopeId: String,
        assetId: String,
        assetKind: ToollyVaultAssetKind
    ) throws -> Data {
        try decrypt(
            envelope: envelope,
            vaultScopeId: vaultScopeId,
            recordId: assetId,
            kind: assetKind.rawValue
        )
    }

    private func encrypt(
        plaintext: Data,
        vaultScopeId: String,
        recordId: String,
        kind: UInt32,
        maxBytes: Int
    ) throws -> Data {
        guard !plaintext.isEmpty, plaintext.count <= maxBytes else {
            throw ToollyVaultCryptoError.invalidEnvelope
        }
        let dataKey = SymmetricKey(size: .bits256)
        var dataKeyBytes = dataKey.withUnsafeBytes { Data($0) }
        defer { dataKeyBytes.resetBytes(in: 0..<dataKeyBytes.count) }

        do {
            let contentAad = toollyVaultAad(
                purpose: .content,
                vaultScopeId: vaultScopeId,
                recordId: recordId,
                kind: kind,
                schemaVersion: toollyVaultSchemaVersion
            )
            guard let combinedContent = try AES.GCM.seal(
                plaintext,
                using: dataKey,
                authenticating: contentAad
            ).combined else {
                throw ToollyVaultCryptoError.platformFailure
            }

            let wrapKey = try wrappingKey.getOrCreate()
            let keyAad = toollyVaultAad(
                purpose: .wrappedKey,
                vaultScopeId: vaultScopeId,
                recordId: recordId,
                kind: kind,
                schemaVersion: toollyVaultSchemaVersion
            )
            guard let combinedKey = try AES.GCM.seal(
                dataKeyBytes,
                using: wrapKey,
                authenticating: keyAad
            ).combined else {
                throw ToollyVaultCryptoError.platformFailure
            }

            return ToollyVaultEnvelopeCodec.encode(wrappedKey: combinedKey, content: combinedContent)
        } catch let error as ToollyVaultCryptoError {
            throw error
        } catch {
            throw ToollyVaultCryptoError.platformFailure
        }
    }

    private func decrypt(
        envelope: Data,
        vaultScopeId: String,
        recordId: String,
        kind: UInt32
    ) throws -> Data {
        let (wrappedKeyCombined, contentCombined) = try ToollyVaultEnvelopeCodec.decode(envelope)
        let wrapKey = try wrappingKey.getExisting()

        let keyAad = toollyVaultAad(
            purpose: .wrappedKey,
            vaultScopeId: vaultScopeId,
            recordId: recordId,
            kind: kind,
            schemaVersion: toollyVaultSchemaVersion
        )
        var dataKeyBytes: Data
        do {
            let sealedKey = try AES.GCM.SealedBox(combined: wrappedKeyCombined)
            dataKeyBytes = try AES.GCM.open(sealedKey, using: wrapKey, authenticating: keyAad)
        } catch let error as ToollyVaultCryptoError {
            throw error
        } catch {
            throw ToollyVaultCryptoError.authenticationFailed
        }
        defer { dataKeyBytes.resetBytes(in: 0..<dataKeyBytes.count) }
        guard dataKeyBytes.count == 32 else { throw ToollyVaultCryptoError.invalidEnvelope }
        let dataKey = SymmetricKey(data: dataKeyBytes)

        let contentAad = toollyVaultAad(
            purpose: .content,
            vaultScopeId: vaultScopeId,
            recordId: recordId,
            kind: kind,
            schemaVersion: toollyVaultSchemaVersion
        )
        do {
            let sealedContent = try AES.GCM.SealedBox(combined: contentCombined)
            return try AES.GCM.open(sealedContent, using: dataKey, authenticating: contentAad)
        } catch let error as ToollyVaultCryptoError {
            throw error
        } catch {
            throw ToollyVaultCryptoError.authenticationFailed
        }
    }
}

/// Non-secret, per-install identifier mixed into every AAD above (`vaultScopeId`) so ciphertext
/// from one vault instance can never be swapped into another's -- mirrors
/// `EncryptedDocumentRepository.loadOrCreateVaultScope()`. Stored in plaintext deliberately: it is
/// a domain-separation value, not a secret.
enum ToollyVaultScope {
    static func loadOrCreate(at url: URL) throws -> String {
        if let existing = try? String(contentsOf: url, encoding: .utf8),
           let normalized = UUID(uuidString: existing.trimmingCharacters(in: .whitespacesAndNewlines)) {
            return normalized.uuidString.lowercased()
        }
        let scope = UUID().uuidString.lowercased()
        try scope.write(to: url, atomically: true, encoding: .utf8)
        return scope
    }
}
