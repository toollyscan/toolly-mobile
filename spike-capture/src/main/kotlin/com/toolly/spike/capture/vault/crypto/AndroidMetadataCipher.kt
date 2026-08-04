package com.toolly.spike.capture.vault.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Android platform-only authenticated encryption for bounded metadata payloads.
 *
 * Every record receives a unique random data key. Android Keystore wraps that key; no key or
 * plaintext is logged, persisted directly, returned to callers or sent to a provider.
 */
internal class AndroidMetadataCipher(
    private val keyAlias: String = DEFAULT_KEY_ALIAS,
    private val random: SecureRandom = SecureRandom(),
) {
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    fun encrypt(
        plaintext: ByteArray,
        associatedData: MetadataAssociatedData,
    ): ByteArray {
        require(plaintext.isNotEmpty())
        require(plaintext.size <= MAX_METADATA_BYTES)
        val dataKeyBytes = ByteArray(MetadataEnvelopeCodec.DATA_KEY_BYTES).also { random.nextBytes(it) }
        return try {
            val contentNonce = randomNonce()
            val contentCipher = Cipher.getInstance(TRANSFORMATION)
            contentCipher.init(
                Cipher.ENCRYPT_MODE,
                SecretKeySpec(dataKeyBytes, KeyProperties.KEY_ALGORITHM_AES),
                GCMParameterSpec(GCM_TAG_BITS, contentNonce),
            )
            contentCipher.updateAAD(associatedData.encode(AadPurpose.CONTENT))
            val ciphertext = contentCipher.doFinal(plaintext)

            val wrappingCipher = Cipher.getInstance(TRANSFORMATION)
            // Android Keystore rejects caller-provided IVs for encryption when the wrapping key
            // requires randomized encryption. Let the provider generate the nonce, then persist it.
            wrappingCipher.init(Cipher.ENCRYPT_MODE, getOrCreateWrappingKey())
            val wrappingNonce = wrappingCipher.iv?.copyOf()
                ?: throw VaultCryptoException.PlatformFailure()
            if (wrappingNonce.size != MetadataEnvelopeCodec.GCM_NONCE_BYTES) {
                wrappingNonce.fill(0)
                throw VaultCryptoException.PlatformFailure()
            }
            wrappingCipher.updateAAD(associatedData.encode(AadPurpose.WRAPPED_KEY))
            val wrappedDataKey = wrappingCipher.doFinal(dataKeyBytes)
            MetadataEnvelopeCodec.encode(
                MetadataEnvelope(
                    wrappingNonce = wrappingNonce,
                    wrappedDataKey = wrappedDataKey,
                    contentNonce = contentNonce,
                    ciphertext = ciphertext,
                ),
            )
        } catch (known: VaultCryptoException) {
            throw known
        } catch (_: GeneralSecurityException) {
            throw VaultCryptoException.PlatformFailure()
        } finally {
            dataKeyBytes.fill(0)
        }
    }

    fun decrypt(
        encodedEnvelope: ByteArray,
        associatedData: MetadataAssociatedData,
    ): ByteArray {
        val envelope = MetadataEnvelopeCodec.decode(encodedEnvelope)
        val wrappingKey = getExistingWrappingKey()
        val dataKeyBytes = try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                wrappingKey,
                GCMParameterSpec(GCM_TAG_BITS, envelope.wrappingNonce),
            )
            cipher.updateAAD(associatedData.encode(AadPurpose.WRAPPED_KEY))
            cipher.doFinal(envelope.wrappedDataKey)
        } catch (_: AEADBadTagException) {
            throw VaultCryptoException.AuthenticationFailed()
        } catch (_: GeneralSecurityException) {
            throw VaultCryptoException.KeyUnavailable()
        }
        if (dataKeyBytes.size != MetadataEnvelopeCodec.DATA_KEY_BYTES) {
            dataKeyBytes.fill(0)
            throw VaultCryptoException.InvalidEnvelope()
        }
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(dataKeyBytes, KeyProperties.KEY_ALGORITHM_AES),
                GCMParameterSpec(GCM_TAG_BITS, envelope.contentNonce),
            )
            cipher.updateAAD(associatedData.encode(AadPurpose.CONTENT))
            cipher.doFinal(envelope.ciphertext)
        } catch (_: AEADBadTagException) {
            throw VaultCryptoException.AuthenticationFailed()
        } catch (_: GeneralSecurityException) {
            throw VaultCryptoException.PlatformFailure()
        } finally {
            dataKeyBytes.fill(0)
        }
    }

    internal fun deleteWrappingKeyForTesting() {
        synchronized(KEYSTORE_LOCK) {
            keyStore.deleteEntry(keyAlias)
        }
    }

    private fun randomNonce(): ByteArray =
        ByteArray(MetadataEnvelopeCodec.GCM_NONCE_BYTES).also { random.nextBytes(it) }

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

    private companion object {
        val KEYSTORE_LOCK = Any()
        const val DEFAULT_KEY_ALIAS = "com.toolly.vault.metadata.wrap.v1"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_BITS = 256
        const val GCM_TAG_BITS = 128
        const val MAX_METADATA_BYTES = 256 * 1024
    }
}

internal sealed class VaultCryptoException : GeneralSecurityException() {
    class InvalidEnvelope : VaultCryptoException()
    class UnsupportedEnvelope : VaultCryptoException()
    class AuthenticationFailed : VaultCryptoException()
    class KeyUnavailable : VaultCryptoException()
    class PlatformFailure : VaultCryptoException()
}
