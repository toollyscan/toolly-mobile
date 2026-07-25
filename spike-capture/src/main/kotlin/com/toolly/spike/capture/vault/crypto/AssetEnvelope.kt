package com.toolly.spike.capture.vault.crypto

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

internal data class AssetAssociatedData(
    val vaultScopeId: String,
    val assetId: String,
    val objectKind: AssetObjectKind,
) {
    init {
        require(CANONICAL_ID.matches(vaultScopeId))
        require(CANONICAL_ID.matches(assetId))
    }

    fun encode(
        purpose: AssetAadPurpose,
        chunkIndex: Int,
        chunkCount: Int,
        totalPlaintextBytes: Long,
    ): ByteArray = java.io.ByteArrayOutputStream().use { bytes ->
        DataOutputStream(bytes).use { output ->
            output.writeInt(AAD_VERSION)
            output.writeInt(purpose.code)
            output.writeUtf8(vaultScopeId)
            output.writeUtf8(assetId)
            output.writeInt(objectKind.code)
            output.writeInt(AssetEnvelopeCodec.VERSION)
            output.writeInt(chunkIndex)
            output.writeInt(chunkCount)
            output.writeLong(totalPlaintextBytes)
        }
        bytes.toByteArray()
    }

    private fun DataOutputStream.writeUtf8(value: String) {
        val encoded = value.toByteArray(Charsets.UTF_8)
        writeInt(encoded.size)
        write(encoded)
    }

    private companion object {
        val CANONICAL_ID =
            Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
        const val AAD_VERSION = 1
    }
}

internal enum class AssetObjectKind(val code: Int) {
    SOURCE_IMAGE(1),
    PROCESSED_IMAGE(2),
    THUMBNAIL(3),
    PDF(4),
}

internal enum class AssetAadPurpose(val code: Int) {
    WRAPPED_KEY(1),
    CONTENT_CHUNK(2),
}

internal data class AssetEnvelopeHeader(
    val chunkPlaintextBytes: Int,
    val totalPlaintextBytes: Long,
    val chunkCount: Int,
    val wrappingNonce: ByteArray,
    val wrappedDataKey: ByteArray,
)

internal object AssetEnvelopeCodec {
    const val VERSION = 1
    const val GCM_NONCE_BYTES = 12
    const val GCM_TAG_BYTES = 16
    const val DATA_KEY_BYTES = 32
    const val WRAPPED_KEY_BYTES = DATA_KEY_BYTES + GCM_TAG_BYTES
    const val DEFAULT_CHUNK_BYTES = 64 * 1024
    const val MAX_ASSET_BYTES = 25L * 1024L * 1024L

    fun writeHeader(output: DataOutputStream, header: AssetEnvelopeHeader) {
        require(header.chunkPlaintextBytes == DEFAULT_CHUNK_BYTES)
        require(header.totalPlaintextBytes in 1..MAX_ASSET_BYTES)
        require(header.chunkCount == chunkCount(header.totalPlaintextBytes))
        require(header.wrappingNonce.size == GCM_NONCE_BYTES)
        require(header.wrappedDataKey.size == WRAPPED_KEY_BYTES)
        output.writeInt(MAGIC)
        output.writeInt(VERSION)
        output.writeInt(header.chunkPlaintextBytes)
        output.writeLong(header.totalPlaintextBytes)
        output.writeInt(header.chunkCount)
        output.writeInt(header.wrappingNonce.size)
        output.writeInt(header.wrappedDataKey.size)
        output.write(header.wrappingNonce)
        output.write(header.wrappedDataKey)
    }

    fun readHeader(input: DataInputStream): AssetEnvelopeHeader = try {
        if (input.readInt() != MAGIC) throw VaultCryptoException.InvalidEnvelope()
        if (input.readInt() != VERSION) throw VaultCryptoException.UnsupportedEnvelope()
        val chunkBytes = input.readInt()
        val totalBytes = input.readLong()
        val chunks = input.readInt()
        val wrappingNonceLength = input.readInt()
        val wrappedKeyLength = input.readInt()
        if (
            chunkBytes != DEFAULT_CHUNK_BYTES ||
            totalBytes !in 1..MAX_ASSET_BYTES ||
            chunks != chunkCount(totalBytes) ||
            wrappingNonceLength != GCM_NONCE_BYTES ||
            wrappedKeyLength != WRAPPED_KEY_BYTES
        ) {
            throw VaultCryptoException.InvalidEnvelope()
        }
        AssetEnvelopeHeader(
            chunkPlaintextBytes = chunkBytes,
            totalPlaintextBytes = totalBytes,
            chunkCount = chunks,
            wrappingNonce = ByteArray(wrappingNonceLength).also { input.readFully(it) },
            wrappedDataKey = ByteArray(wrappedKeyLength).also { input.readFully(it) },
        )
    } catch (known: VaultCryptoException) {
        throw known
    } catch (_: IOException) {
        throw VaultCryptoException.InvalidEnvelope()
    }

    fun chunkCount(totalPlaintextBytes: Long): Int =
        ((totalPlaintextBytes + DEFAULT_CHUNK_BYTES - 1) / DEFAULT_CHUNK_BYTES).toInt()

    private const val MAGIC = 0x544C5941
}
