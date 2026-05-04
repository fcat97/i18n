package com.portonics.i18n.google

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SheetsClientTest {

    @Test
    fun extractSheetIdFromFullUrl() {
        val url = "https://docs.google.com/spreadsheets/d/abc123def456ghi789jkl012mno345pqr678stu901vw/edit"
        val result = SheetsClient.extractSheetId(url)
        assertEquals("abc123def456ghi789jkl012mno345pqr678stu901vw", result)
    }

    @Test
    fun extractSheetIdFromShortUrl() {
        val url = "https://docs.google.com/spreadsheets/d/abc123def456ghi789jkl012mno345pqr678stu901vw/edit#gid=0"
        val result = SheetsClient.extractSheetId(url)
        assertEquals("abc123def456ghi789jkl012mno345pqr678stu901vw", result)
    }

    @Test
    fun extractSheetIdFromBareSheetId() {
        val sheetId = "abc123def456ghi789jkl012mno345pqr678stu901vwxyz"
        val result = SheetsClient.extractSheetId(sheetId)
        assertEquals(sheetId, result)
    }

    @Test
    fun extractSheetIdReturnsNullForInvalidUrl() {
        val url = "https://example.com/spreadsheet/123"
        val result = SheetsClient.extractSheetId(url)
        assertNull(result)
    }

    @Test
    fun extractSheetIdFromUrlWithoutSpreadsheetPath() {
        val url = "https://docs.google.com/spreadsheets/d/abc123def/edit"
        val result = SheetsClient.extractSheetId(url)
        assertEquals("abc123def", result)
    }
}