package com.toolly.spike.capture.vault.metadata

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Protects the random SQLCipher passphrase with an Android Keystore key.
 *
 * Missing or corrupt existing key material fails closed. It never silently creates a replacement
 * passphrase for an existing vault.
 */
internal class AndroidVaultPassphraseProtector(
    context: Context,
    private val keyAlias: String = DEFAULT_KEY_ALIAS,
    rootDirectoryName: String = DEFAULT_ROOT_DIRECTORY,
) {
    private val root = File(context.noBackupFilesDir, rootDirectoryName)
    private val envelopeFile = File(root, KEY_ENVELOPE_FILE)
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    private val random = SecureRandom()

    fun loadOrCreate(allowCreate: Boolean): ByteArray = synchronized(KEY_MATERIAL_LOCK) {
        if (envelopeFile.exists()) {
            return@synchronized decryptEnvelope(readEnvelope())
        }
        if (!allowCreate) throw VaultKeyMaterialException.MissingEnvelope()

        root.mkdirsOrThrow()
        val wrappingKey = getOrCreateWrappingKey()
        val passphrase = ByteArray(PASSPHRASE_BYTES).also { random.nextBytes(it) }
        try {
            writeEnvelope(encryptPassphrase(wrappingKey, passphrase))
            passphrase
        } catch (error: Exception) {
            passphrase.fill(0)
            throw error
        }
    }

    internal fun envelopeFileForTesting(): File = envelopeFile

    internal fun deleteKeyForTesting() {
        synchronized(KEY_MATERIAL_LOCK) {
            keyStore.deleteEntry(keyAlias)
        }
    }

    private fun getOrCreateWrappingKey(): SecretKey {
        (keyStore.getKey(keyAlias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE,
        )
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
        return generator.generateKey()
    }

    private fun encryptPassphrase(
        wrappingKey: SecretKey,
        passphrase: ByteArray,
    ): KeyEnvelope {
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, wrappingKey)
        cipher.updateAAD(KEY_ENVELOPE_AAD)
        return KeyEnvelope(
            iv = cipher.iv.copyOf(),
            ciphertext = cipher.doFinal(passphrase),
        )
    }

    private fun decryptEnvelope(envelope: KeyEnvelope): ByteArray {
        val wrappingKey = keyStore.getKey(keyAlias, null) as? SecretKey
            ?: throw VaultKeyMaterialException.KeyUnavailable()
        return try {
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, wrappingKey, GCMParameterSpec(GCM_TAG_BITS, envelope.iv))
            cipher.updateAAD(KEY_ENVELOPE_AAD)
            cipher.doFinal(envelope.ciphertext).also {
                if (it.size != PASSPHRASE_BYTES) {
                    it.fill(0)
                    throw VaultKeyMaterialException.CorruptEnvelope()
                }
            }
        } catch (_: AEADBadTagException) {
            throw VaultKeyMaterialException.CorruptEnvelope()
        } catch (_: GeneralSecurityException) {
            throw VaultKeyMaterialException.KeyUnavailable()
        }
    }

    private fun writeEnvelope(envelope: KeyEnvelope) {
        val encoded = ByteArrayOutputStream().use { bytes ->
            DataOutputStream(bytes).use { output ->
                output.writeInt(ENVELOPE_MAGIC)
                output.writeInt(ENVELOPE_VERSION)
                output.writeInt(envelope.iv.size)
                output.writeInt(envelope.ciphertext.size)
                output.write(envelope.iv)
                output.write(envelope.ciphertext)
            }
            bytes.toByteArray()
        }
        val temporary = File(root, "$KEY_ENVELOPE_FILE.part")
        try {
            FileOutputStream(temporary).use { output ->
                output.write(encoded)
                output.fd.sync()
            }
            if (!temporary.renameTo(envelopeFile)) throw IOException()
        } finally {
            encoded.fill(0)
            temporary.delete()
        }
    }

    private fun readEnvelope(): KeyEnvelope {
        if (!envelopeFile.isFile || envelopeFile.length() !in MIN_ENVELOPE_BYTES..MAX_ENVELOPE_BYTES) {
            throw VaultKeyMaterialException.CorruptEnvelope()
        }
        return try {
            DataInputStream(ByteArrayInputStream(envelopeFile.readBytes())).use { input ->
                if (input.readInt() != ENVELOPE_MAGIC) throw VaultKeyMaterialException.CorruptEnvelope()
                if (input.readInt() != ENVELOPE_VERSION) {
                    throw VaultKeyMaterialException.UnsupportedEnvelope()
                }
                val ivLength = input.readInt()
                val ciphertextLength = input.readInt()
                if (ivLength != GCM_IV_BYTES || ciphertextLength != ENCRYPTED_PASSPHRASE_BYTES) {
                    throw VaultKeyMaterialException.CorruptEnvelope()
                }
                val iv = ByteArray(ivLength).also { input.readFully(it) }
                val ciphertext = ByteArray(ciphertextLength).also { input.readFully(it) }
                if (input.read() != -1) throw VaultKeyMaterialException.CorruptEnvelope()
                KeyEnvelope(iv, ciphertext)
            }
        } catch (known: VaultKeyMaterialException) {
            throw known
        } catch (_: IOException) {
            throw VaultKeyMaterialException.CorruptEnvelope()
        }
    }

    private fun File.mkdirsOrThrow() {
        if ((!exists() && !mkdirs()) || !isDirectory) throw VaultKeyMaterialException.StorageFailure()
    }

    private data class KeyEnvelope(
        val iv: ByteArray,
        val ciphertext: ByteArray,
    )

    private companion object {
        val KEY_MATERIAL_LOCK = Any()
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        const val DEFAULT_KEY_ALIAS = "com.toolly.vault.metadata.wrap.v1"
        const val DEFAULT_ROOT_DIRECTORY = "toolly-vault-candidate-v1"
        const val KEY_ENVELOPE_FILE = "metadata-key-envelope.bin"
        const val ENVELOPE_MAGIC = 0x544C594B
        const val ENVELOPE_VERSION = 1
        const val KEY_BITS = 256
        const val PASSPHRASE_BYTES = 32
        const val GCM_IV_BYTES = 12
        const val GCM_TAG_BITS = 128
        const val ENCRYPTED_PASSPHRASE_BYTES = PASSPHRASE_BYTES + (GCM_TAG_BITS / 8)
        const val MIN_ENVELOPE_BYTES = 16L + GCM_IV_BYTES + ENCRYPTED_PASSPHRASE_BYTES
        const val MAX_ENVELOPE_BYTES = 512L
        val KEY_ENVELOPE_AAD = "toolly|vault|metadata-passphrase|v1".toByteArray(Charsets.UTF_8)
    }
}

internal sealed class VaultKeyMaterialException : GeneralSecurityException() {
    class MissingEnvelope : VaultKeyMaterialException()
    class UnsupportedEnvelope : VaultKeyMaterialException()
    class CorruptEnvelope : VaultKeyMaterialException()
    class KeyUnavailable : VaultKeyMaterialException()
    class StorageFailure : VaultKeyMaterialException()
}
