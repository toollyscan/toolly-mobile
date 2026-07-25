package com.toolly.spike.capture.vault.crypto

import java.util.UUID
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MetadataEnvelopeTest {
    @Test
    fun associatedData_isDeterministicAndPurposeBound() {
        val data = fixtureAssociatedData()
        assertArrayEquals(data.encode(AadPurpose.CONTENT), data.encode(AadPurpose.CONTENT))
        assertNotEquals(
            data.encode(AadPurpose.CONTENT).toList(),
            data.encode(AadPurpose.WRAPPED_KEY).toList(),
        )
    }

    @Test
    fun envelopeCodec_roundTripsAndRejectsTrailingBytes() {
        val envelope = MetadataEnvelope(
            wrappingNonce = ByteArray(12) { 1 },
            wrappedDataKey = ByteArray(48) { 2 },
            contentNonce = ByteArray(12) { 3 },
            ciphertext = ByteArray(32) { 4 },
        )
        val encoded = MetadataEnvelopeCodec.encode(envelope)
        val decoded = MetadataEnvelopeCodec.decode(encoded)
        assertArrayEquals(envelope.wrappingNonce, decoded.wrappingNonce)
        assertArrayEquals(envelope.wrappedDataKey, decoded.wrappedDataKey)
        assertArrayEquals(envelope.contentNonce, decoded.contentNonce)
        assertArrayEquals(envelope.ciphertext, decoded.ciphertext)
        assertThrows(VaultCryptoException.InvalidEnvelope::class.java) {
            MetadataEnvelopeCodec.decode(encoded + byteArrayOf(0))
        }
    }

    private fun fixtureAssociatedData() = MetadataAssociatedData(
        vaultScopeId = UUID.randomUUID().toString(),
        recordId = UUID.randomUUID().toString(),
        recordKind = RecordKind.DOCUMENT,
        schemaVersion = 1,
        revision = 0,
    )
}
