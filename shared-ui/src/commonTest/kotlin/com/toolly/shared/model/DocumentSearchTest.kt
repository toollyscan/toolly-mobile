package com.toolly.shared.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DocumentSearchTest {

    private val invoice = DocumentListItem(DocumentUiId("1"), pageCount = 2, title = "April invoice")
    private val receipt = DocumentListItem(DocumentUiId("2"), pageCount = 1, title = "Travel receipt")
    private val untitled = DocumentListItem(DocumentUiId("3"), pageCount = 3, title = null)
    private val documents = listOf(invoice, receipt, untitled)

    @Test
    fun blankQueryReturnsNoResults() {
        // Matches wireframe 4.2's empty-query "type to search" state -- never "all documents".
        assertTrue(filterDocumentsByTitle(documents, "").isEmpty())
        assertTrue(filterDocumentsByTitle(documents, "   ").isEmpty())
    }

    @Test
    fun matchesTitleCaseInsensitivelyAsSubstring() {
        assertEquals(listOf(invoice), filterDocumentsByTitle(documents, "invoice"))
        assertEquals(listOf(invoice), filterDocumentsByTitle(documents, "APRIL"))
    }

    @Test
    fun matchesAcrossMultipleDocuments() {
        assertEquals(listOf(invoice, receipt), filterDocumentsByTitle(documents, "e"))
    }

    @Test
    fun noMatchReturnsEmpty() {
        assertTrue(filterDocumentsByTitle(documents, "passport").isEmpty())
    }

    @Test
    fun untitledDocumentsNeverMatch() {
        assertTrue(filterDocumentsByTitle(listOf(untitled), "anything").isEmpty())
    }
}
