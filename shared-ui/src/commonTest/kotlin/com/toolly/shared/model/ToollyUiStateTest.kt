package com.toolly.shared.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ToollyUiStateTest {
    @Test
    fun emptyStateStartsAtLibraryWithoutFixtureContent() {
        val state = ToollyUiState.empty()

        assertEquals(ToollyDestination.LIBRARY, state.destination)
        assertEquals(emptyList(), state.documents)
        assertEquals(0, state.reviewPageCount)
    }

    @Test
    fun negativeReviewPageCountIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            ToollyUiState.empty().copy(reviewPageCount = -1)
        }
    }
}
