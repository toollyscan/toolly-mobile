package com.toolly.spike.capture.vault.metadata

import androidx.test.core.app.ApplicationProvider
import java.io.File
import java.util.UUID
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class AndroidVaultPassphraseProtectorInstrumentedTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private lateinit var alias: String
    private lateinit var rootName: String
    private lateinit var protector: AndroidVaultPassphraseProtector

    @Before
    fun setUp() {
        val suffix = UUID.randomUUID().toString()
        alias = "com.toolly.test.vault.$suffix"
        rootName = "toolly-vault-test-$suffix"
        protector = AndroidVaultPassphraseProtector(context, alias, rootName)
    }

    @After
    fun tearDown() {
        protector.deleteKeyForTesting()
        File(context.noBackupFilesDir, rootName).deleteRecursively()
    }

    @Test
    fun passphrase_isStableAcrossProtectorInstances() {
        val first = protector.loadOrCreate(allowCreate = true)
        val second = AndroidVaultPassphraseProtector(context, alias, rootName)
            .loadOrCreate(allowCreate = false)
        try {
            assertArrayEquals(first, second)
        } finally {
            first.fill(0)
            second.fill(0)
        }
    }

    @Test
    fun corruptEnvelope_failsClosedWithoutReplacement() {
        protector.loadOrCreate(allowCreate = true).fill(0)
        val envelope = protector.envelopeFileForTesting()
        envelope.writeBytes(ByteArray(32) { it.toByte() })
        val corrupted = envelope.readBytes()

        assertThrows(VaultKeyMaterialException.CorruptEnvelope::class.java) {
            protector.loadOrCreate(allowCreate = false)
        }
        assertArrayEquals(corrupted, envelope.readBytes())
    }

    @Test
    fun missingKeystoreKey_doesNotSilentlyResetExistingEnvelope() {
        protector.loadOrCreate(allowCreate = true).fill(0)
        val envelope = protector.envelopeFileForTesting()
        protector.deleteKeyForTesting()

        assertThrows(VaultKeyMaterialException.KeyUnavailable::class.java) {
            protector.loadOrCreate(allowCreate = false)
        }
        assertFalse(envelope.readBytes().all { it == 0.toByte() })
    }
}
