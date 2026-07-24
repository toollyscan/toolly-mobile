package com.toolly.spike.capture.vault.metadata

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction

@Entity(tableName = "vault_documents")
internal data class VaultDocumentEntity(
    @androidx.room.PrimaryKey
    @ColumnInfo(name = "document_id")
    val documentId: String,
    @ColumnInfo(name = "created_at_epoch_millis")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at_epoch_millis")
    val updatedAtEpochMillis: Long,
    @ColumnInfo(name = "page_count")
    val pageCount: Int,
    @ColumnInfo(name = "lifecycle")
    val lifecycle: String,
    @ColumnInfo(name = "schema_version")
    val schemaVersion: Int,
)

@Entity(
    tableName = "vault_pages",
    foreignKeys = [
        ForeignKey(
            entity = VaultDocumentEntity::class,
            parentColumns = ["document_id"],
            childColumns = ["document_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["document_id", "ordinal"], unique = true),
        Index(value = ["asset_id"], unique = true),
    ],
)
internal data class VaultPageEntity(
    @androidx.room.PrimaryKey
    @ColumnInfo(name = "page_id")
    val pageId: String,
    @ColumnInfo(name = "document_id")
    val documentId: String,
    @ColumnInfo(name = "asset_id")
    val assetId: String,
    @ColumnInfo(name = "ordinal")
    val ordinal: Int,
    @ColumnInfo(name = "width_pixels")
    val widthPixels: Int?,
    @ColumnInfo(name = "height_pixels")
    val heightPixels: Int?,
)

@Dao
internal interface VaultMetadataDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertDocument(document: VaultDocumentEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertPages(pages: List<VaultPageEntity>)

    @Transaction
    fun insertDocumentWithPages(
        document: VaultDocumentEntity,
        pages: List<VaultPageEntity>,
    ) {
        require(document.pageCount == pages.size)
        require(pages.map(VaultPageEntity::ordinal).sorted() == pages.indices.toList())
        insertDocument(document)
        insertPages(pages)
    }

    @Query("SELECT * FROM vault_documents ORDER BY updated_at_epoch_millis DESC, document_id ASC")
    fun listDocuments(): List<VaultDocumentEntity>

    @Query("SELECT * FROM vault_documents WHERE document_id = :documentId LIMIT 1")
    fun getDocument(documentId: String): VaultDocumentEntity?

    @Query("SELECT * FROM vault_pages WHERE document_id = :documentId ORDER BY ordinal ASC")
    fun getPages(documentId: String): List<VaultPageEntity>
}

@Database(
    entities = [VaultDocumentEntity::class, VaultPageEntity::class],
    version = 1,
    exportSchema = true,
)
internal abstract class VaultMetadataDatabase : RoomDatabase() {
    abstract fun metadataDao(): VaultMetadataDao
}
