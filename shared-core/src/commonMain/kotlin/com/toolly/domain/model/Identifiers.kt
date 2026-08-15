package com.toolly.domain.model

import kotlin.jvm.JvmInline

private val LOWER_UUID =
    Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")

private fun requireCanonicalId(value: String) {
    require(LOWER_UUID.matches(value)) { "Toolly IDs must be lower-case UUID strings" }
}

@JvmInline
value class DocumentId(val value: String) {
    init {
        requireCanonicalId(value)
    }
}

@JvmInline
value class PageId(val value: String) {
    init {
        requireCanonicalId(value)
    }
}

@JvmInline
value class AssetId(val value: String) {
    init {
        requireCanonicalId(value)
    }
}

@JvmInline
value class OperationId(val value: String) {
    init {
        requireCanonicalId(value)
    }
}

@JvmInline
value class TemporaryAssetId(val value: String) {
    init {
        require(value.isNotBlank()) { "Temporary asset ID must not be blank" }
        require(value.length <= 128) { "Temporary asset ID is too long" }
        require(value.all { it.isLetterOrDigit() || it == '-' || it == '_' }) {
            "Temporary asset ID contains prohibited characters"
        }
    }
}
