package com.toolly.spike.capture.vault.crypto

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

internal data class MetadataAssociatedData(
    val vaultScopeId: String,
    val recordId: String,
    val recordKind: RecordKind,
    val schemaVersion: Int,
    val revision: Long,
) {
    init {
        require(CANONICAL_ID.matches(vaultScopeId))
        require(CANONICAL_ID.matches(recordId))
        require(schemaVersion > 0)
        require(revision >= 0)
    }

    fun encode(purpose: AadPurpose): ByteArray = ByteArrayOutputStream().use { bytes ->
        DataOutputStream(bytes).use { output ->
            output.writeInt(AAD_VERSION)
            output.writeInt(purpose.code)
            output.writeUtf8(vaultScopeId)
            output.writeUtf8(recordId)
            output.writeInt(recordKind.code)
            output.writeInt(schemaVersion)
            output.writeLong(revision)
        }
        bytes.toByteArray()
    }

    private fun DataOutputStream.writeUtf8(value: String) {
        val encoded = value.toByteArray(Charsets.UTF_8)
        writeInt(encoded.size)
        write(encoded)
    }

    internal companion object {
        private val CANONICAL_ID =
            Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
        private const val AAD_VERSION = 1
    }
}

internal enum class RecordKind(val code: Int) {
    DOCUMENT(1),
    PAGE(2),
    TRANSACTION(3),
}

internal enum class AadPurpose(val code: Int) {
    WRAPPED_KEY(1),
    CONTENT(2),
}

internal data class MetadataEnvelope(
    val wrappingNonce: ByteArray,
    val wrappedDataKey: ByteArray,
    val contentNonce: ByteArray,
    val ciphertext: ByteArray,
)

internal object MetadataEnvelopeCodec {
    fun encode(envelope: MetadataEnvelope): ByteArray {
        require(envelope.wrappingNonce.size == GCM_NONCE_BYTES)
        require(envelope.contentNonce.size == GCM_NONCE_BYTES)
        require(envelope.wrappedDataKey.size == WRAPPED_KEY_BYTES)
        require(envelope.ciphertext.size in GCM_TAG_BYTES..MAX_CIPHERTEXT_BYTES)
        return ByteArrayOutputStream().use { bytes ->
            DataOutputStream(bytes).use { output ->
                output.writeInt(MAGIC)
                output.writeInt(VERSION)
                output.writeInt(envelope.wrappingNonce.size)
                output.writeInt(envelope.wrappedDataKey.size)
                output.writeInt(envelope.contentNonce.size)
                output.writeInt(envelope.ciphertext.size)
                output.write(envelope.wrappingNonce)
                output.write(envelope.wrappedDataKey)
                output.write(envelope.contentNonce)
                output.write(envelope.ciphertext)
            }
            bytes.toByteArray()
        }
    }

    fun decode(encoded: ByteArray): MetadataEnvelope {
        if (encoded.size !in MIN_ENVELOPE_BYTES..MAX_ENVELOPE_BYTES) {
            throw VaultCryptoException.InvalidEnvelope()
        }
        return try {
            DataInputStream(ByteArrayInputStream(encoded)).use { input ->
                if (input.readInt() != MAGIC) throw VaultCryptoException.InvalidEnvelope()
                if (input.readInt() != VERSION) throw VaultCryptoException.UnsupportedEnvelope()
                val wrappingNonceLength = input.readInt()
                val wrappedKeyLength = input.readInt()
                val contentNonceLength = input.readInt()
                val ciphertextLength = input.readInt()
                if (
                    wrappingNonceLength != GCM_NONCE_BYTES ||
                    wrappedKeyLength != WRAPPED_KEY_BYTES ||
                    contentNonceLength != GCM_NONCE_BYTES ||
                    ciphertextLength !in GCM_TAG_BYTES..MAX_CIPHERTEXT_BYTES
                ) {
                    throw VaultCryptoException.InvalidEnvelope()
                }
                val expected = HEADER_BYTES + wrappingNonceLength + wrappedKeyLength +
                    contentNonceLength + ciphertextLength
                if (expected != encoded.size) throw VaultCryptoException.InvalidEnvelope()
                val wrappingNonce = ByteArray(wrappingNonceLength).also { input.readFully(it) }
                val wrappedKey = ByteArray(wrappedKeyLength).also { input.readFully(it) }
                val contentNonce = ByteArray(contentNonceLength).also { input.readFully(it) }
                val ciphertext = ByteArray(ciphertextLength).also { input.readFully(it) }
                if (input.read() != -1) throw VaultCryptoException.InvalidEnvelope()
                MetadataEnvelope(wrappingNonce, wrappedKey, contentNonce, ciphertext)
            }
        } catch (known: VaultCryptoException) {
            throw known
        } catch (_: IOException) {
            throw VaultCryptoException.InvalidEnvelope()
        }
    }

    private const val MAGIC = 0x544C594D
    private const val VERSION = 1
    private const val HEADER_BYTES = 6 * Int.SIZE_BYTES
    internal const val GCM_NONCE_BYTES = 12
    internal const val GCM_TAG_BYTES = 16
    internal const val DATA_KEY_BYTES = 32
    private const val WRAPPED_KEY_BYTES = DATA_KEY_BYTES + GCM_TAG_BYTES
    private const val MAX_PLAINTEXT_BYTES = 256 * 1024
    private const val MAX_CIPHERTEXT_BYTES = MAX_PLAINTEXT_BYTES + GCM_TAG_BYTES
    private const val MIN_ENVELOPE_BYTES =
        HEADER_BYTES + GCM_NONCE_BYTES + WRAPPED_KEY_BYTES + GCM_NONCE_BYTES + GCM_TAG_BYTES
    private const val MAX_ENVELOPE_BYTES =
        HEADER_BYTES + GCM_NONCE_BYTES + WRAPPED_KEY_BYTES + GCM_NONCE_BYTES + MAX_CIPHERTEXT_BYTES
}
