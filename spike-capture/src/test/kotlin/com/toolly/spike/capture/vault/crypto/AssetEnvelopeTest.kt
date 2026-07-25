package com.toolly.spike.capture.vault.crypto

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.UUID
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AssetEnvelopeTest {
    @Test
    fun associatedData_isDeterministicAndBindsPurposeAndChunkPosition() {
        val data = fixtureAssociatedData()
        val first = data.encode(AssetAadPurpose.CONTENT_CHUNK, 0, 2, 100)
        assertArrayEquals(first, data.encode(AssetAadPurpose.CONTENT_CHUNK, 0, 2, 100))
        assertNotEquals(
            first.toList(),
            data.encode(AssetAadPurpose.CONTENT_CHUNK, 1, 2, 100).toList(),
        )
        assertNotEquals(
            first.toList(),
            data.encode(AssetAadPurpose.WRAPPED_KEY, -1, 2, 100).toList(),
        )
    }

    @Test
    fun headerCodec_roundTripsAndRejectsInconsistentChunkCount() {
        val header = AssetEnvelopeHeader(
            chunkPlaintextBytes = AssetEnvelopeCodec.DEFAULT_CHUNK_BYTES,
            totalPlaintextBytes = AssetEnvelopeCodec.DEFAULT_CHUNK_BYTES.toLong() + 1,
            chunkCount = 2,
            wrappingNonce = ByteArray(AssetEnvelopeCodec.GCM_NONCE_BYTES) { 1 },
            wrappedDataKey = ByteArray(AssetEnvelopeCodec.WRAPPED_KEY_BYTES) { 2 },
        )
        val encoded = ByteArrayOutputStream().use { bytes ->
            DataOutputStream(bytes).use { AssetEnvelopeCodec.writeHeader(it, header) }
            bytes.toByteArray()
        }
        val decoded = DataInputStream(ByteArrayInputStream(encoded)).use {
            AssetEnvelopeCodec.readHeader(it)
        }
        assertEquals(header.chunkPlaintextBytes, decoded.chunkPlaintextBytes)
        assertEquals(header.totalPlaintextBytes, decoded.totalPlaintextBytes)
        assertEquals(header.chunkCount, decoded.chunkCount)
        assertArrayEquals(header.wrappingNonce, decoded.wrappingNonce)
        assertArrayEquals(header.wrappedDataKey, decoded.wrappedDataKey)

        val invalid = encoded.copyOf().also {
            val chunkCountOffset = Int.SIZE_BYTES * 3 + Long.SIZE_BYTES
            it[chunkCountOffset + Int.SIZE_BYTES - 1] = 3
        }
        assertThrows(VaultCryptoException.InvalidEnvelope::class.java) {
            DataInputStream(ByteArrayInputStream(invalid)).use {
                AssetEnvelopeCodec.readHeader(it)
            }
        }
    }

    private fun fixtureAssociatedData() = AssetAssociatedData(
        vaultScopeId = UUID.randomUUID().toString(),
        assetId = UUID.randomUUID().toString(),
        objectKind = AssetObjectKind.SOURCE_IMAGE,
    )
}
