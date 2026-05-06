package io.github.fcat97.i18n.google

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import java.io.File

class SheetsClientTest {

    @Test
    fun extractSheetIdFromFullUrl() {
        val url = "https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgVE2upms/edit"
        val result = SheetsClient.extractSheetId(url)
        assertEquals("1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgVE2upms", result)
    }

    @Test
    fun extractSheetIdFromShortUrl() {
        val url = "https://docs.google.com/spreadsheets/d/abc123def456/edit#gid=0"
        val result = SheetsClient.extractSheetId(url)
        assertEquals("abc123def456", result)
    }

    @Test
    fun extractSheetIdReturnsNullForInvalidUrl() {
        val url = "https://example.com/not-a-sheet"
        val result = SheetsClient.extractSheetId(url)
        assertEquals(null, result)
    }

    @Test
    fun getCacheFileReturnsDeterministicPath() {
        val cacheDir = File(System.getProperty("java.io.tmpdir"), "test-cache")
        val url = "https://docs.google.com/spreadsheets/d/abc123/edit"

        val file1 = SheetsClient.getCacheFile(cacheDir, url)
        val file2 = SheetsClient.getCacheFile(cacheDir, url)

        assertEquals(file1.absolutePath, file2.absolutePath)
    }

    @Test
    fun deleteCacheRemovesExistingFile() {
        val cacheDir = File(System.getProperty("java.io.tmpdir"), "test-cache-delete")
        cacheDir.mkdirs()
        val url = "https://docs.google.com/spreadsheets/d/test123/edit"
        val cacheFile = SheetsClient.getCacheFile(cacheDir, url)
        cacheFile.writeText("cached content")
        assertTrue(cacheFile.exists())

        SheetsClient.deleteCache(cacheDir, url)

        assertTrue(!cacheFile.exists())
    }
}
