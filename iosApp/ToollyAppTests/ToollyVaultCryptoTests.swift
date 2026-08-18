import CryptoKit
import XCTest
@testable import ToollyApp

/// Real correctness evidence for `ToollyVaultCrypto.swift`, not just "it compiles" -- the gap
/// this repo's own CryptoKit/Keychain Swift code has had since it was first written (see #101/
/// #102's own history: two genuine bugs in this vault slice sat merged, invisible, for over a
/// week because the file wasn't even in the build). These run against the real iOS Keychain in
/// the test host's simulator, not a mock -- as close to "does this actually work" as this
/// environment gets without a physical-device pass.
///
/// Each test uses its own wrapping-key account (`UUID` per test) so tests never share Keychain
/// state or leak between runs.
final class ToollyVaultCryptoTests: XCTestCase {
    private func makeCipher() -> (cipher: ToollyVaultCipher, key: ToollyVaultWrappingKey) {
        let key = ToollyVaultWrappingKey(account: "test-\(UUID().uuidString)")
        return (ToollyVaultCipher(wrappingKey: key), key)
    }

    override func tearDown() {
        // Keychain entries created by getOrCreate() persist past the test unless removed --
        // clean up defensively even though each test uses a fresh, never-reused account.
        super.tearDown()
    }

    func testMetadataRoundTrip() throws {
        let (cipher, key) = makeCipher()
        defer { key.deleteForTesting() }
        let plaintext = Data("{\"documentId\":\"doc-1\"}".utf8)

        let envelope = try cipher.encryptMetadata(
            plaintext: plaintext,
            vaultScopeId: UUID().uuidString,
            recordId: "doc-1",
            recordKind: .document
        )
        XCTAssertNotEqual(envelope, plaintext, "ciphertext must not equal plaintext")

        let scopeId = UUID().uuidString
        let opened = try cipher.decryptMetadata(
            envelope: try cipher.encryptMetadata(
                plaintext: plaintext,
                vaultScopeId: scopeId,
                recordId: "doc-1",
                recordKind: .document
            ),
            vaultScopeId: scopeId,
            recordId: "doc-1",
            recordKind: .document
        )
        XCTAssertEqual(opened, plaintext)
    }

    func testAssetRoundTrip() throws {
        let (cipher, key) = makeCipher()
        defer { key.deleteForTesting() }
        let plaintext = Data((0..<4096).map { UInt8($0 % 256) }) // synthetic bounded "page" bytes
        let scopeId = UUID().uuidString

        let envelope = try cipher.encryptAsset(
            plaintext: plaintext,
            vaultScopeId: scopeId,
            assetId: "asset-1",
            assetKind: .sourceImage
        )
        let opened = try cipher.decryptAsset(
            envelope: envelope,
            vaultScopeId: scopeId,
            assetId: "asset-1",
            assetKind: .sourceImage
        )
        XCTAssertEqual(opened, plaintext)
    }

    func testDecryptFailsWhenRecordIdDoesNotMatch() throws {
        let (cipher, key) = makeCipher()
        defer { key.deleteForTesting() }
        let scopeId = UUID().uuidString
        let envelope = try cipher.encryptMetadata(
            plaintext: Data("secret".utf8),
            vaultScopeId: scopeId,
            recordId: "doc-a",
            recordKind: .document
        )

        // Same key, same scope, wrong record id -- must not decrypt. This is exactly the
        // "ciphertext swapped between records" attack the AAD binding exists to prevent.
        XCTAssertThrowsError(
            try cipher.decryptMetadata(
                envelope: envelope,
                vaultScopeId: scopeId,
                recordId: "doc-b",
                recordKind: .document
            )
        ) { error in
            XCTAssertEqual(error as? ToollyVaultCryptoError, .authenticationFailed)
        }
    }

    func testDecryptFailsWhenVaultScopeDoesNotMatch() throws {
        let (cipher, key) = makeCipher()
        defer { key.deleteForTesting() }
        let envelope = try cipher.encryptAsset(
            plaintext: Data("page bytes".utf8),
            vaultScopeId: "scope-a",
            assetId: "asset-1",
            assetKind: .sourceImage
        )

        XCTAssertThrowsError(
            try cipher.decryptAsset(
                envelope: envelope,
                vaultScopeId: "scope-b",
                assetId: "asset-1",
                assetKind: .sourceImage
            )
        ) { error in
            XCTAssertEqual(error as? ToollyVaultCryptoError, .authenticationFailed)
        }
    }

    func testDecryptFailsOnCorruptedEnvelope() throws {
        let (cipher, key) = makeCipher()
        defer { key.deleteForTesting() }
        let scopeId = UUID().uuidString
        var envelope = try cipher.encryptMetadata(
            plaintext: Data("secret".utf8),
            vaultScopeId: scopeId,
            recordId: "doc-1",
            recordKind: .document
        )
        // Flip a byte well inside the ciphertext -- must fail authentication, not silently
        // decrypt to garbage.
        envelope[envelope.count - 1] ^= 0xFF

        XCTAssertThrowsError(
            try cipher.decryptMetadata(
                envelope: envelope,
                vaultScopeId: scopeId,
                recordId: "doc-1",
                recordKind: .document
            )
        )
    }

    func testDecryptFailsOnTruncatedEnvelope() throws {
        let (cipher, key) = makeCipher()
        defer { key.deleteForTesting() }
        let scopeId = UUID().uuidString
        let envelope = try cipher.encryptMetadata(
            plaintext: Data("secret".utf8),
            vaultScopeId: scopeId,
            recordId: "doc-1",
            recordKind: .document
        )

        XCTAssertThrowsError(
            try cipher.decryptMetadata(
                envelope: envelope.prefix(envelope.count / 2),
                vaultScopeId: scopeId,
                recordId: "doc-1",
                recordKind: .document
            )
        ) { error in
            XCTAssertEqual(error as? ToollyVaultCryptoError, .invalidEnvelope)
        }
    }

    func testDecryptFailsWhenWrappingKeyIsMissing() throws {
        let (cipher, key) = makeCipher()
        let scopeId = UUID().uuidString
        let envelope = try cipher.encryptMetadata(
            plaintext: Data("secret".utf8),
            vaultScopeId: scopeId,
            recordId: "doc-1",
            recordKind: .document
        )
        // Simulates the wrapping key vanishing from the Keychain (e.g. a restore that doesn't
        // carry Keychain items) -- must fail closed as keyUnavailable, distinct from a corrupt/
        // tampered-ciphertext failure, since the ciphertext here is perfectly intact.
        key.deleteForTesting()

        XCTAssertThrowsError(
            try cipher.decryptMetadata(
                envelope: envelope,
                vaultScopeId: scopeId,
                recordId: "doc-1",
                recordKind: .document
            )
        ) { error in
            XCTAssertEqual(error as? ToollyVaultCryptoError, .keyUnavailable)
        }
    }

    func testEncryptRejectsEmptyPlaintext() {
        let (cipher, key) = makeCipher()
        defer { key.deleteForTesting() }

        XCTAssertThrowsError(
            try cipher.encryptMetadata(
                plaintext: Data(),
                vaultScopeId: UUID().uuidString,
                recordId: "doc-1",
                recordKind: .document
            )
        ) { error in
            XCTAssertEqual(error as? ToollyVaultCryptoError, .invalidEnvelope)
        }
    }

    func testEncryptRejectsOversizedMetadata() {
        let (cipher, key) = makeCipher()
        defer { key.deleteForTesting() }
        let oversized = Data(repeating: 0, count: 300 * 1024 + 1)

        XCTAssertThrowsError(
            try cipher.encryptMetadata(
                plaintext: oversized,
                vaultScopeId: UUID().uuidString,
                recordId: "doc-1",
                recordKind: .document
            )
        ) { error in
            XCTAssertEqual(error as? ToollyVaultCryptoError, .invalidEnvelope)
        }
    }

    func testWrappingKeyPersistsAcrossInstances() throws {
        let account = "test-\(UUID().uuidString)"
        let first = ToollyVaultWrappingKey(account: account)
        defer { first.deleteForTesting() }
        let created = try first.getOrCreate()

        // A second instance over the same Keychain account must resolve to the *same* key --
        // this is what makes decrypting a document saved in an earlier app launch possible at
        // all, since the vault re-creates its ToollyVaultWrappingKey fresh on every launch.
        let second = ToollyVaultWrappingKey(account: account)
        let resolved = try second.getExisting()
        XCTAssertEqual(created.withUnsafeBytes { Data($0) }, resolved.withUnsafeBytes { Data($0) })
    }

    func testVaultScopeLoadOrCreatePersists() throws {
        let directory = FileManager.default.temporaryDirectory
            .appendingPathComponent(UUID().uuidString, isDirectory: true)
        try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
        defer { try? FileManager.default.removeItem(at: directory) }
        let scopeFile = directory.appendingPathComponent("vault.scope")

        let first = try ToollyVaultScope.loadOrCreate(at: scopeFile)
        let second = try ToollyVaultScope.loadOrCreate(at: scopeFile)
        XCTAssertEqual(first, second, "a second load must return the persisted scope, not mint a new one")
        XCTAssertNotNil(UUID(uuidString: first))
    }
}
