package io.github.fcat97.i18n.parser

data class LocalizationEntry(
    val key: String,
    val platform: String,
    val defaultValue: String,
    val translations: Map<String, String>
)

data class LocalizationData(
    val defaultLocale: String,
    val additionalLocales: List<String>,
    val entries: List<LocalizationEntry>
)

object ExcelParser {
    private val REQUIRED_HEADERS = listOf("key", "platform")
    private const val DEFAULT_COLUMN = "default"

    fun parse(csvContent: String): LocalizationData {
        val lines = csvContent.lines().filter { it.isNotBlank() }

        if (lines.isEmpty()) {
            return LocalizationData(DEFAULT_COLUMN, emptyList(), emptyList())
        }

        val headers = parseCsvLine(lines[0])
        validateHeaders(headers)

        val defaultIndex = headers.indexOf(DEFAULT_COLUMN)
        val additionalLocales = headers.filter { it != DEFAULT_COLUMN && it !in REQUIRED_HEADERS }
        val entries = lines.drop(1).mapNotNull { line ->
            parseEntry(parseCsvLine(line), defaultIndex, additionalLocales)
        }

        return LocalizationData(DEFAULT_COLUMN, additionalLocales, entries)
    }

    private fun validateHeaders(headers: List<String>) {
        REQUIRED_HEADERS.forEach { required ->
            if (required !in headers) {
                throw IllegalArgumentException("Missing required header: $required")
            }
        }
        if (DEFAULT_COLUMN !in headers) {
            throw IllegalArgumentException("Missing required header: $DEFAULT_COLUMN")
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var remaining = line

        while (remaining.isNotEmpty()) {
            if (remaining.startsWith("\"")) {
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

    private fun parseEntry(values: List<String>, defaultIndex: Int, additionalLocales: List<String>): LocalizationEntry? {
        if (values.isEmpty()) return null

        val key = values[0].trim()
        if (key.isEmpty()) return null

        val platform = if (values.size > 1) values[1].trim().lowercase() else ""
        val defaultValue = if (defaultIndex >= 0 && defaultIndex < values.size) values[defaultIndex].trim() else ""

        val translations = mutableMapOf<String, String>()
        additionalLocales.forEachIndexed { localeIndex, locale ->
            val valueIndex = 3 + localeIndex
            if (valueIndex < values.size) {
                translations[locale] = values[valueIndex].trim()
            }
        }

        return LocalizationEntry(key, platform, defaultValue, translations)
    }
}
