package com.toolly.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class IdentifiersTest {
    @Test
    fun `canonical lower-case UUID is accepted`() {
        val value = "123e4567-e89b-12d3-a456-426614174000"

        assertEquals(value, DocumentId(value).value)
    }

    @Test
    fun `provider path and upper-case UUID are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            DocumentId("users/account/documents/1")
        }
        assertThrows(IllegalArgumentException::class.java) {
            DocumentId("123E4567-E89B-12D3-A456-426614174000")
        }
    }
}
