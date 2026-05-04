package com.portonics.i18n.generator

import com.portonics.i18n.parser.LocalizationData
import com.portonics.i18n.parser.LocalizationEntry
import java.io.File
import java.nio.file.Files

object AndroidStringsGenerator {
    private const val PLATFORM_ANDROID = "android"

    fun generate(data: LocalizationData, outputDir: File) {
        if (outputDir.exists()) {
            outputDir.deleteRecursively()
        }
        outputDir.mkdirs()

        val androidEntries = data.entries.filter { it.platform.contains(PLATFORM_ANDROID) }

        generateLocaleStrings(data.defaultLocale, androidEntries, outputDir, data.defaultLocale)

        data.additionalLocales.forEach { locale ->
            generateLocaleStrings(locale, androidEntries, outputDir, data.defaultLocale)
        }
    }

    private fun generateLocaleStrings(
        locale: String,
        entries: List<LocalizationEntry>,
        outputDir: File,
        defaultLocale: String
    ) {
        val localeDir = if (locale == defaultLocale || locale == "default") {
            File(outputDir, "values")
        } else {
            File(outputDir, "values-$locale")
        }

        localeDir.mkdirs()
        val stringsFile = File(localeDir, "strings.xml")

        val content = buildString {
            appendLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
            appendLine("<resources>")
            entries.forEach { entry ->
                val translation = entry.translations[locale] ?: entry.defaultValue
                if (!translation.isNullOrEmpty()) {
                    val escapedKey = escapeXml(entry.key)
                    val escapedValue = escapeXml(translation)
                    appendLine("    <string name=\"$escapedKey\">$escapedValue</string>")
                }
            }
            appendLine("</resources>")
        }

        Files.write(stringsFile.toPath(), content.toByteArray())
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun StringBuilder.appendLine(line: String) {
        append(line)
        append("\n")
    }
}