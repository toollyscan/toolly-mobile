package com.toolly.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IdentifiersTest {
    @Test
    fun canonicalLowerCaseUuidIsAccepted() {
        val value = "123e4567-e89b-12d3-a456-426614174000"

        assertEquals(value, DocumentId(value).value)
    }

    @Test
    fun providerPathAndUpperCaseUuidAreRejected() {
        assertFailsWith<IllegalArgumentException> {
            DocumentId("users/account/documents/1")
        }
        assertFailsWith<IllegalArgumentException> {
            DocumentId("123E4567-E89B-12D3-A456-426614174000")
        }
    }
}
