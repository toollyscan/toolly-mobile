package com.toolly.spike.capture.vault.metadata

import android.content.Context
import androidx.room.Room
import java.io.File
import net.zetetic.database.Logger
import net.zetetic.database.NoopTarget
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

/** Android-only SQLCipher adapter. No SQLCipher type crosses this package boundary. */
internal class SqlCipherVaultMetadataFactory(
    private val context: Context,
    private val protector: AndroidVaultPassphraseProtector =
        AndroidVaultPassphraseProtector(context),
    rootDirectoryName: String = DEFAULT_ROOT_DIRECTORY,
) {
    private val root = File(context.noBackupFilesDir, rootDirectoryName)
    private val databaseFile = File(root, DATABASE_FILE)

    fun open(): VaultMetadataDatabase {
        root.mkdirsOrThrow()
        val passphrase = protector.loadOrCreate(allowCreate = !databaseFile.exists())
        return try {
            loadSqlCipher()
            val database = Room.databaseBuilder(
                context,
                VaultMetadataDatabase::class.java,
                databaseFile.absolutePath,
            )
                .openHelperFactory(SupportOpenHelperFactory(passphrase))
                .setJournalMode(androidx.room.RoomDatabase.JournalMode.TRUNCATE)
                .build()
            database.openHelper.writableDatabase
            database
        } finally {
            passphrase.fill(0)
        }
    }

    internal fun databaseFileForTesting(): File = databaseFile

    private fun loadSqlCipher() {
        synchronized(SQLCIPHER_LOCK) {
            if (sqlCipherLoaded) return
            System.loadLibrary(SQLCIPHER_LIBRARY)
            Logger.setTarget(NoopTarget())
            sqlCipherLoaded = true
        }
    }

    private fun File.mkdirsOrThrow() {
        if ((!exists() && !mkdirs()) || !isDirectory) throw VaultKeyMaterialException.StorageFailure()
    }

    private companion object {
        val SQLCIPHER_LOCK = Any()
        const val DEFAULT_ROOT_DIRECTORY = "toolly-vault-candidate-v1"
        const val DATABASE_FILE = "vault-metadata.db"
        const val SQLCIPHER_LIBRARY = "sqlcipher"
        @Volatile
        var sqlCipherLoaded: Boolean = false
    }
}
