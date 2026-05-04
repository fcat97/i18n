package com.portonics.i18n.google

import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object SheetsClient {
    fun downloadWithCredentials(sheetUrl: String, credentialsFile: File): String {
        throw UnsupportedOperationException("Service account authentication requires credentials.json. Please provide credentials file.")
    }

    fun downloadPublicSheet(sheetUrl: String): String {
        return try {
            val sheetId = extractSheetId(sheetUrl)
                ?: throw IllegalArgumentException("Invalid Google Sheet URL: $sheetUrl")

            val exportUrl = "https://docs.google.com/spreadsheets/d/$sheetId/export?format=csv"

            val url = URL(exportUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            val responseCode = connection.responseCode
            if (responseCode != 200) {
                throw RuntimeException("HTTP error: $responseCode")
            }

            val content = InputStreamReader(connection.inputStream).use { it.readText() }
            connection.disconnect()

            content
        } catch (e: Exception) {
            throw RuntimeException("Failed to download Google Sheet: ${e.message}", e)
        }
    }

    fun extractSheetId(url: String): String? {
        val pattern = Regex("/spreadsheets/d/([a-zA-Z0-9-_]+)")
        return pattern.find(url)?.groupValues?.getOrNull(1)
            ?: if (url.length >= 44 && !url.contains("/")) url else null
    }
}