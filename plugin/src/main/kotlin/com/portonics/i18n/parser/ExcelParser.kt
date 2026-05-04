package com.portonics.i18n.parser

data class LocalizationEntry(
    val key: String,
    val platform: String,
    val translations: Map<String, String>
)

data class LocalizationData(
    val locales: List<String>,
    val entries: List<LocalizationEntry>
)

object ExcelParser {
    private val REQUIRED_HEADERS = listOf("key", "platform")

    fun parse(csvContent: String): LocalizationData {
        val lines = csvContent.lines().filter { it.isNotBlank() }

        if (lines.isEmpty()) {
            return LocalizationData(emptyList(), emptyList())
        }

        val headers = parseCsvLine(lines[0])
        validateHeaders(headers)

        val locales = headers.filter { it !in REQUIRED_HEADERS }
        val entries = lines.drop(1).mapNotNull { line -> parseEntry(parseCsvLine(line), locales) }

        return LocalizationData(locales, entries)
    }

    private fun validateHeaders(headers: List<String>) {
        REQUIRED_HEADERS.forEach { required ->
            if (required !in headers) {
                throw IllegalArgumentException("Missing required header: $required")
            }
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var remaining = line
        var inQuotes = false

        while (remaining.isNotEmpty()) {
            if (remaining.startsWith("\"")) {
                inQuotes = true
                remaining = remaining.substring(1)
                val endQuote = remaining.indexOf("\"")
                if (endQuote >= 0) {
                    result.add(remaining.substring(0, endQuote))
                    remaining = if (endQuote + 1 < remaining.length) remaining.substring(endQuote + 1) else ""
                    if (remaining.startsWith(",")) remaining = remaining.substring(1)
                } else {
                    result.add(remaining)
                    remaining = ""
                }
            } else {
                val commaIndex = remaining.indexOf(",")
                if (commaIndex >= 0) {
                    result.add(remaining.substring(0, commaIndex))
                    remaining = remaining.substring(commaIndex + 1)
                } else {
                    result.add(remaining)
                    remaining = ""
                }
            }
        }
        return result
    }

    private fun parseEntry(values: List<String>, locales: List<String>): LocalizationEntry? {
        if (values.isEmpty()) return null

        val key = values[0].trim()
        if (key.isEmpty()) {
            return null
        }

        val platform = if (values.size > 1) values[1].trim().lowercase() else ""
        val translations = locales.associateWith { locale ->
            val index = locales.indexOf(locale) + 2
            if (index < values.size) values[index].trim() else ""
        }

        return LocalizationEntry(key, platform, translations)
    }
}