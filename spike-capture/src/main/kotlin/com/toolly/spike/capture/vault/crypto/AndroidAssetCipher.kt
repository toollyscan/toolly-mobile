package com.toolly.spike.capture.vault.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/** Android platform AES-GCM adapter for independently authenticated, bounded asset chunks. */
internal class AndroidAssetCipher(
    private val keyAlias: String = DEFAULT_KEY_ALIAS,
    private val random: SecureRandom = SecureRandom(),
) {
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    fun encrypt(
        source: File,
        target: File,
        associatedData: AssetAssociatedData,
    ) {
        if (!source.isFile || source.length() !in 1..AssetEnvelopeCodec.MAX_ASSET_BYTES) {
            throw VaultCryptoException.InvalidEnvelope()
        }
        val totalBytes = source.length()
        val chunkCount = AssetEnvelopeCodec.chunkCount(totalBytes)
        val dataKeyBytes = ByteArray(AssetEnvelopeCodec.DATA_KEY_BYTES).also(random::nextBytes)
        try {
            val wrappedDataKey = wrapDataKey(
                dataKeyBytes = dataKeyBytes,
                associatedData = associatedData,
                chunkCount = chunkCount,
                totalBytes = totalBytes,
            )
            FileInputStream(source).use { sourceStream ->
                FileOutputStream(target).use { fileOutput ->
                    DataOutputStream(BufferedOutputStream(fileOutput)).use { output ->
                        AssetEnvelopeCodec.writeHeader(
                            output,
                            AssetEnvelopeHeader(
                                chunkPlaintextBytes = AssetEnvelopeCodec.DEFAULT_CHUNK_BYTES,
                                totalPlaintextBytes = totalBytes,
                                chunkCount = chunkCount,
                                wrappingNonce = wrappedDataKey.nonce,
                                wrappedDataKey = wrappedDataKey.ciphertext,
                            ),
                        )
                        val buffer = ByteArray(AssetEnvelopeCodec.DEFAULT_CHUNK_BYTES)
                        var chunkIndex = 0
                        var consumed = 0L
                        while (consumed < totalBytes) {
                            val count = sourceStream.readChunk(buffer)
                            if (count <= 0 || chunkIndex >= chunkCount) {
                                throw VaultCryptoException.InvalidEnvelope()
                            }
                            val nonce = randomNonce()
                            val cipher = Cipher.getInstance(TRANSFORMATION)
                            cipher.init(
                                Cipher.ENCRYPT_MODE,
                                SecretKeySpec(dataKeyBytes, KeyProperties.KEY_ALGORITHM_AES),
                                GCMParameterSpec(GCM_TAG_BITS, nonce),
                            )
                            cipher.updateAAD(
                                associatedData.encode(
                                    purpose = AssetAadPurpose.CONTENT_CHUNK,
                                    chunkIndex = chunkIndex,
                                    chunkCount = chunkCount,
                                    totalPlaintextBytes = totalBytes,
                                ),
                            )
                            val ciphertext = cipher.doFinal(buffer, 0, count)
                            output.writeInt(chunkIndex)
                            output.writeInt(count)
                            output.writeInt(nonce.size)
                            output.writeInt(ciphertext.size)
                            output.write(nonce)
                            output.write(ciphertext)
                            consumed += count
                            chunkIndex += 1
                        }
                        if (consumed != totalBytes || chunkIndex != chunkCount) {
                            throw VaultCryptoException.InvalidEnvelope()
                        }
                        output.flush()
                        fileOutput.fd.sync()
                    }
                }
            }
        } catch (known: VaultCryptoException) {
            target.delete()
            throw known
        } catch (_: GeneralSecurityException) {
            target.delete()
            throw VaultCryptoException.PlatformFailure()
        } catch (_: IOException) {
            target.delete()
            throw VaultCryptoException.InvalidEnvelope()
        } finally {
            dataKeyBytes.fill(0)
        }
    }

    fun openDecrypted(
        encryptedFile: File,
        associatedData: AssetAssociatedData,
    ): InputStream {
        if (!encryptedFile.isFile) throw VaultCryptoException.InvalidEnvelope()
        val input = try {
            DataInputStream(BufferedInputStream(FileInputStream(encryptedFile)))
        } catch (_: IOException) {
            throw VaultCryptoException.InvalidEnvelope()
        }
        return try {
            val header = AssetEnvelopeCodec.readHeader(input)
            val dataKey = unwrapDataKey(header, associatedData)
            ChunkDecryptingInputStream(input, dataKey, header, associatedData)
        } catch (failure: Exception) {
            input.close()
            throw failure
        }
    }

    fun verify(
        encryptedFile: File,
        associatedData: AssetAssociatedData,
    ) {
        openDecrypted(encryptedFile, associatedData).use { input ->
            val buffer = ByteArray(AssetEnvelopeCodec.DEFAULT_CHUNK_BYTES)
            while (input.read(buffer) != -1) {
                // Authentication occurs while each bounded chunk is read.
            }
        }
    }

    internal fun deleteWrappingKeyForTesting() {
        synchronized(KEYSTORE_LOCK) {
            keyStore.deleteEntry(keyAlias)
        }
    }

    private fun wrapDataKey(
        dataKeyBytes: ByteArray,
        associatedData: AssetAssociatedData,
        chunkCount: Int,
        totalBytes: Long,
    ): WrappedDataKey {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        // Android Keystore rejects caller-provided IVs for encryption when the wrapping key
        // requires randomized encryption. Let the provider generate the nonce, then persist it.
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateWrappingKey())
        val nonce = cipher.iv?.copyOf()
            ?: throw VaultCryptoException.PlatformFailure()
        if (nonce.size != AssetEnvelopeCodec.GCM_NONCE_BYTES) {
            nonce.fill(0)
            throw VaultCryptoException.PlatformFailure()
        }
        cipher.updateAAD(
            associatedData.encode(
                purpose = AssetAadPurpose.WRAPPED_KEY,
                chunkIndex = WRAPPED_KEY_CHUNK_INDEX,
                chunkCount = chunkCount,
                totalPlaintextBytes = totalBytes,
            ),
        )
        return WrappedDataKey(
            nonce = nonce,
            ciphertext = cipher.doFinal(dataKeyBytes),
        )
    }

    private fun unwrapDataKey(
        header: AssetEnvelopeHeader,
        associatedData: AssetAssociatedData,
    ): ByteArray = try {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getExistingWrappingKey(),
            GCMParameterSpec(GCM_TAG_BITS, header.wrappingNonce),
        )
        cipher.updateAAD(
            associatedData.encode(
                purpose = AssetAadPurpose.WRAPPED_KEY,
                chunkIndex = WRAPPED_KEY_CHUNK_INDEX,
                chunkCount = header.chunkCount,
                totalPlaintextBytes = header.totalPlaintextBytes,
            ),
        )
        cipher.doFinal(header.wrappedDataKey).also {
            if (it.size != AssetEnvelopeCodec.DATA_KEY_BYTES) {
                it.fill(0)
                throw VaultCryptoException.InvalidEnvelope()
            }
        }
    } catch (_: AEADBadTagException) {
        throw VaultCryptoException.AuthenticationFailed()
    } catch (known: VaultCryptoException) {
        throw known
    } catch (_: GeneralSecurityException) {
        throw VaultCryptoException.KeyUnavailable()
    }

    private fun randomNonce(): ByteArray =
        ByteArray(AssetEnvelopeCodec.GCM_NONCE_BYTES).also(random::nextBytes)

    private fun getExistingWrappingKey(): SecretKey = synchronized(KEYSTORE_LOCK) {
        keyStore.getKey(keyAlias, null) as? SecretKey
            ?: throw VaultCryptoException.KeyUnavailable()
    }

    private fun getOrCreateWrappingKey(): SecretKey = synchronized(KEYSTORE_LOCK) {
        (keyStore.getKey(keyAlias, null) as? SecretKey)?.let { return@synchronized it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_BITS)
                .setRandomizedEncryptionRequired(true)
                .build(),
        )
        generator.generateKey()
    }

    private fun InputStream.readChunk(buffer: ByteArray): Int {
        var total = 0
        while (total < buffer.size) {
            val count = read(buffer, total, buffer.size - total)
            if (count < 0) break
            if (count == 0) continue
            total += count
        }
        return total
    }

    private class ChunkDecryptingInputStream(
        private val source: DataInputStream,
        private val dataKeyBytes: ByteArray,
        private val header: AssetEnvelopeHeader,
        private val associatedData: AssetAssociatedData,
    ) : InputStream() {
        private var currentChunk = ByteArray(0)
        private var currentOffset = 0
        private var nextChunkIndex = 0
        private var plaintextBytesRead = 0L
        private var completed = false
        private var closed = false

        override fun read(): Int {
            val one = ByteArray(1)
            return if (read(one, 0, 1) == -1) -1 else one[0].toInt() and 0xFF
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            checkBounds(buffer.size, offset, length)
            if (length == 0) return 0
            if (closed) throw IOException("Stream is closed")
            if (currentOffset >= currentChunk.size && !loadNextChunk()) return -1
            val count = minOf(length, currentChunk.size - currentOffset)
            currentChunk.copyInto(buffer, offset, currentOffset, currentOffset + count)
            currentOffset += count
            return count
        }

        override fun close() {
            if (!closed) {
                closed = true
                currentChunk.fill(0)
                dataKeyBytes.fill(0)
                source.close()
            }
        }

        private fun loadNextChunk(): Boolean {
            currentChunk.fill(0)
            currentChunk = ByteArray(0)
            currentOffset = 0
            if (completed) return false
            if (nextChunkIndex == header.chunkCount) {
                if (
                    plaintextBytesRead != header.totalPlaintextBytes ||
                    source.read() != -1
                ) {
                    throw VaultCryptoException.InvalidEnvelope()
                }
                completed = true
                return false
            }
            try {
                val encodedIndex = source.readInt()
                val plaintextLength = source.readInt()
                val nonceLength = source.readInt()
                val ciphertextLength = source.readInt()
                val expectedPlaintextLength = minOf(
                    AssetEnvelopeCodec.DEFAULT_CHUNK_BYTES.toLong(),
                    header.totalPlaintextBytes - plaintextBytesRead,
                ).toInt()
                if (
                    encodedIndex != nextChunkIndex ||
                    plaintextLength != expectedPlaintextLength ||
                    nonceLength != AssetEnvelopeCodec.GCM_NONCE_BYTES ||
                    ciphertextLength != plaintextLength + AssetEnvelopeCodec.GCM_TAG_BYTES
                ) {
                    throw VaultCryptoException.InvalidEnvelope()
                }
                val nonce = ByteArray(nonceLength).also { source.readFully(it) }
                val ciphertext = ByteArray(ciphertextLength).also { source.readFully(it) }
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    SecretKeySpec(dataKeyBytes, KeyProperties.KEY_ALGORITHM_AES),
                    GCMParameterSpec(GCM_TAG_BITS, nonce),
                )
                cipher.updateAAD(
                    associatedData.encode(
                        purpose = AssetAadPurpose.CONTENT_CHUNK,
                        chunkIndex = nextChunkIndex,
                        chunkCount = header.chunkCount,
                        totalPlaintextBytes = header.totalPlaintextBytes,
                    ),
                )
                currentChunk = cipher.doFinal(ciphertext)
                if (currentChunk.size != plaintextLength) {
                    currentChunk.fill(0)
                    throw VaultCryptoException.InvalidEnvelope()
                }
                plaintextBytesRead += currentChunk.size
                nextChunkIndex += 1
                return true
            } catch (_: AEADBadTagException) {
                throw VaultCryptoException.AuthenticationFailed()
            } catch (known: VaultCryptoException) {
                throw known
            } catch (_: GeneralSecurityException) {
                throw VaultCryptoException.PlatformFailure()
            } catch (_: IOException) {
                throw VaultCryptoException.InvalidEnvelope()
            }
        }

        private fun checkBounds(size: Int, offset: Int, length: Int) {
            if (offset < 0 || length < 0 || offset > size - length) {
                throw IndexOutOfBoundsException()
            }
        }
    }

    private data class WrappedDataKey(
        val nonce: ByteArray,
        val ciphertext: ByteArray,
    )

    private companion object {
        val KEYSTORE_LOCK = Any()
        const val DEFAULT_KEY_ALIAS = "com.toolly.vault.asset.wrap.v1"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_BITS = 256
        const val GCM_TAG_BITS = 128
        const val WRAPPED_KEY_CHUNK_INDEX = -1
    }
}
