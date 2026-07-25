package com.toolly.spike.capture.vault.crypto

import java.util.UUID
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class AndroidMetadataCipherInstrumentedTest {
    private lateinit var alias: String
    private lateinit var cipher: AndroidMetadataCipher

    @Before
    fun setUp() {
        alias = "com.toolly.test.metadata.${UUID.randomUUID()}"
        cipher = AndroidMetadataCipher(alias)
    }

    @After
    fun tearDown() {
        cipher.deleteWrappingKeyForTesting()
    }

    @Test
    fun encryptedMetadata_reopensThroughASeparateAdapterInstance() {
        val plaintext = "instrumentation-only metadata fixture".toByteArray()
        val aad = fixtureAssociatedData()
        val encrypted = cipher.encrypt(plaintext, aad)
        val reopened = AndroidMetadataCipher(alias)

        assertFalse(encrypted.toList().windowed(plaintext.size).any { it == plaintext.toList() })
        assertArrayEquals(plaintext, reopened.decrypt(encrypted, aad))
    }

    @Test
    fun tamperAndAssociatedDataSubstitution_failAuthentication() {
        val aad = fixtureAssociatedData()
        val encrypted = cipher.encrypt(ByteArray(64) { it.toByte() }, aad)
        val tampered = encrypted.copyOf().also { it[it.lastIndex] = (it.last() + 1).toByte() }
        assertThrows(VaultCryptoException.AuthenticationFailed::class.java) {
            cipher.decrypt(tampered, aad)
        }

        val substituted = aad.copy(revision = aad.revision + 1)
        assertThrows(VaultCryptoException.AuthenticationFailed::class.java) {
            cipher.decrypt(encrypted, substituted)
        }
    }

    @Test
    fun missingWrappingKey_failsClosed() {
        val aad = fixtureAssociatedData()
        val encrypted = cipher.encrypt(ByteArray(32) { 7 }, aad)
        cipher.deleteWrappingKeyForTesting()

        assertThrows(VaultCryptoException.KeyUnavailable::class.java) {
            cipher.decrypt(encrypted, aad)
        }
    }

    @Test
    fun repeatedEncryption_usesDistinctEnvelopes() {
        val aad = fixtureAssociatedData()
        val plaintext = ByteArray(32) { 9 }
        val first = cipher.encrypt(plaintext, aad)
        val second = cipher.encrypt(plaintext, aad)

        assertNotEquals(first.toList(), second.toList())
        assertArrayEquals(plaintext, cipher.decrypt(first, aad))
        assertArrayEquals(plaintext, cipher.decrypt(second, aad))
    }

    private fun fixtureAssociatedData() = MetadataAssociatedData(
        vaultScopeId = UUID.randomUUID().toString(),
        recordId = UUID.randomUUID().toString(),
        recordKind = RecordKind.DOCUMENT,
        schemaVersion = 1,
        revision = 0,
    )
}
