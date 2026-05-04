package com.portonics.i18n.google

import java.io.File
import java.io.InputStreamReader
import java.security.MessageDigest
import java.net.HttpURLConnection
import java.net.URL

object SheetsClient {
    private const val CACHE_DIR_NAME = "i18n-cache"

    fun getCacheFile(cacheDir: File, sheetUrl: String): File {
        val md5Hash = md5(sheetUrl)
        return File(cacheDir, "$md5Hash.csv")
    }

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        val sb = StringBuilder()
        for (b in digest) {
            sb.append("%02x".format(b))
        }
        return sb.toString()
    }

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

    fun downloadWithCache(sheetUrl: String, cacheDir: File): String {
        val cacheFile = getCacheFile(cacheDir, sheetUrl)

        if (cacheFile.exists()) {
            return cacheFile.readText()
        }

        val content = downloadPublicSheet(sheetUrl)

        cacheDir.mkdirs()
        cacheFile.writeText(content)

        return content
    }

    fun deleteCache(cacheDir: File, sheetUrl: String) {
        val cacheFile = getCacheFile(cacheDir, sheetUrl)
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
    }

    fun extractSheetId(url: String): String? {
        val pattern = Regex("/spreadsheets/d/([a-zA-Z0-9-_]+)")
        return pattern.find(url)?.groupValues?.getOrNull(1)
            ?: if (url.length >= 44 && !url.contains("/")) url else null
    }
}