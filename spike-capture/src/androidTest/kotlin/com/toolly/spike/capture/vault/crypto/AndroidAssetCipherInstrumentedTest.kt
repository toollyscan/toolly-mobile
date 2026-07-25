package com.toolly.spike.capture.vault.crypto

import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.RandomAccessFile
import java.util.UUID
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class AndroidAssetCipherInstrumentedTest {
    private lateinit var alias: String
    private lateinit var cipher: AndroidAssetCipher
    private lateinit var workingDirectory: File

    @Before
    fun setUp() {
        alias = "com.toolly.test.asset.${UUID.randomUUID()}"
        cipher = AndroidAssetCipher(alias)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        workingDirectory = File(context.cacheDir, UUID.randomUUID().toString()).also {
            check(it.mkdirs())
        }
    }

    @After
    fun tearDown() {
        cipher.deleteWrappingKeyForTesting()
        workingDirectory.deleteRecursively()
    }

    @Test
    fun multiChunkAsset_reopensWithoutPersistingPlaintext() {
        val plaintext = ByteArray(AssetEnvelopeCodec.DEFAULT_CHUNK_BYTES + 257) {
            (it % 251).toByte()
        }
        val source = File(workingDirectory, "source.bin").apply { writeBytes(plaintext) }
        val encrypted = File(workingDirectory, "asset.tlya")
        val aad = fixtureAssociatedData()

        cipher.encrypt(source, encrypted, aad)
        val reopened = AndroidAssetCipher(alias)
        val decrypted = reopened.openDecrypted(encrypted, aad).use { it.readBytes() }

        assertFalse(encrypted.readBytes().containsSubsequence(plaintext.copyOfRange(0, 512)))
        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun tamperTruncationAndAssociatedDataSubstitution_failClosed() {
        val source = File(workingDirectory, "source.bin").apply {
            writeBytes(ByteArray(AssetEnvelopeCodec.DEFAULT_CHUNK_BYTES + 1) { 7 })
        }
        val encrypted = File(workingDirectory, "asset.tlya")
        val aad = fixtureAssociatedData()
        cipher.encrypt(source, encrypted, aad)
        val original = encrypted.readBytes()

        RandomAccessFile(encrypted, "rw").use {
            val offset = it.length() - 1
            it.seek(offset)
            val value = it.read()
            it.seek(offset)
            it.write(value.xor(1))
        }
        assertThrows(VaultCryptoException::class.java) {
            cipher.openDecrypted(encrypted, aad).use { it.readBytes() }
        }

        encrypted.writeBytes(original.copyOf(original.size - 1))
        assertThrows(VaultCryptoException.InvalidEnvelope::class.java) {
            cipher.openDecrypted(encrypted, aad).use { it.readBytes() }
        }

        encrypted.writeBytes(original)
        assertThrows(VaultCryptoException.AuthenticationFailed::class.java) {
            cipher.openDecrypted(
                encrypted,
                aad.copy(assetId = UUID.randomUUID().toString()),
            ).use { it.readBytes() }
        }
    }

    @Test
    fun missingWrappingKey_doesNotResetOrDecryptAsset() {
        val source = File(workingDirectory, "source.bin").apply {
            writeBytes(ByteArray(128) { 3 })
        }
        val encrypted = File(workingDirectory, "asset.tlya")
        val aad = fixtureAssociatedData()
        cipher.encrypt(source, encrypted, aad)
        cipher.deleteWrappingKeyForTesting()

        assertThrows(VaultCryptoException.KeyUnavailable::class.java) {
            cipher.openDecrypted(encrypted, aad)
        }
    }

    private fun fixtureAssociatedData() = AssetAssociatedData(
        vaultScopeId = UUID.randomUUID().toString(),
        assetId = UUID.randomUUID().toString(),
        objectKind = AssetObjectKind.SOURCE_IMAGE,
    )

    private fun ByteArray.containsSubsequence(candidate: ByteArray): Boolean =
        asList().windowed(candidate.size).any { it == candidate.asList() }
}
