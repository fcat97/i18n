package com.portonics.i18n.generator

import com.portonics.i18n.parser.LocalizationData
import com.portonics.i18n.parser.LocalizationEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.io.File

class AndroidStringsGeneratorTest {

    @Test
    fun generateCreatesCorrectDirectoryStructure() {
        val tempDir = createTempDir()
        val data = LocalizationData(
            locales = listOf("en", "my", "zh"),
            entries = listOf(
                LocalizationEntry("app_name", "android", mapOf("en" to "My App", "my" to "My App MM", "zh" to "My App CN"))
            )
        )

        AndroidStringsGenerator.generate(data, tempDir, "en")

        assertTrue(File(tempDir, "values").exists())
        assertTrue(File(tempDir, "values-my").exists())
        assertTrue(File(tempDir, "values-zh").exists())
    }

    @Test
    fun generateCreatesValidXmlFiles() {
        val tempDir = createTempDir()
        val data = LocalizationData(
            locales = listOf("en", "my"),
            entries = listOf(
                LocalizationEntry("app_name", "android", mapOf("en" to "My App", "my" to "My App MM")),
                LocalizationEntry("welcome_text", "android", mapOf("en" to "Welcome", "my" to "Welcome MM"))
            )
        )

        AndroidStringsGenerator.generate(data, tempDir, "en")

        val enStrings = File(tempDir, "values/strings.xml").readText()
        assertTrue(enStrings.contains("<?xml version=\"1.0\" encoding=\"utf-8\"?>"))
        assertTrue(enStrings.contains("<resources>"))
        assertTrue(enStrings.contains("</resources>"))
        assertTrue(enStrings.contains("name=\"app_name\""))
        assertTrue(enStrings.contains("name=\"welcome_text\""))
    }

    @Test
    fun generateFiltersEntriesByPlatform() {
        val tempDir = createTempDir()
        val data = LocalizationData(
            locales = listOf("en"),
            entries = listOf(
                LocalizationEntry("android_key", "android", mapOf("en" to "Android Value")),
                LocalizationEntry("ios_key", "ios", mapOf("en" to "iOS Value")),
                LocalizationEntry("both", "android,ios", mapOf("en" to "Both Value"))
            )
        )

        AndroidStringsGenerator.generate(data, tempDir, "en")

        val enStrings = File(tempDir, "values/strings.xml").readText()
        assertTrue(enStrings.contains("Android Value"))
        assertTrue(enStrings.contains("Both Value"))
        assertTrue(!enStrings.contains("iOS Value"))
    }

    @Test
    fun generateUsesValuesDirectoryForDefaultLocale() {
        val tempDir = createTempDir()
        val data = LocalizationData(
            locales = listOf("en", "my"),
            entries = listOf(
                LocalizationEntry("app_name", "android", mapOf("en" to "My App", "my" to "My App MM"))
            )
        )

        AndroidStringsGenerator.generate(data, tempDir, "en")

        assertTrue(File(tempDir, "values/strings.xml").exists())
    }

    @Test
    fun generateUsesValuesLocaleDirectoryForNonDefaultLocales() {
        val tempDir = createTempDir()
        val data = LocalizationData(
            locales = listOf("en", "my"),
            entries = listOf(
                LocalizationEntry("app_name", "android", mapOf("en" to "My App", "my" to "My App MM"))
            )
        )

        AndroidStringsGenerator.generate(data, tempDir, "en")

        assertTrue(File(tempDir, "values-my/strings.xml").exists())
    }

    @Test
    fun generateEscapesXmlSpecialCharacters() {
        val tempDir = createTempDir()
        val data = LocalizationData(
            locales = listOf("en"),
            entries = listOf(
                LocalizationEntry("ampersand", "android", mapOf("en" to "A & B")),
                LocalizationEntry("less_than", "android", mapOf("en" to "A < B")),
                LocalizationEntry("greater_than", "android", mapOf("en" to "A > B")),
                LocalizationEntry("quote", "android", mapOf("en" to "A \"B\"")),
                LocalizationEntry("apostrophe", "android", mapOf("en" to "A 'B'"))
            )
        )

        AndroidStringsGenerator.generate(data, tempDir, "en")

        val enStrings = File(tempDir, "values/strings.xml").readText()
        assertTrue(enStrings.contains("&amp;"))
        assertTrue(enStrings.contains("&lt;"))
        assertTrue(enStrings.contains("&gt;"))
        assertTrue(enStrings.contains("&quot;"))
        assertTrue(enStrings.contains("&apos;"))
        assertTrue(!enStrings.contains("A & B"))
        assertTrue(!enStrings.contains("A < B"))
    }

    @Test
    fun generateSkipsEmptyTranslations() {
        val tempDir = createTempDir()
        val entries = listOf(
            LocalizationEntry("valid_key", "android", mapOf("en" to "Value", "my" to "")),
            LocalizationEntry("empty_key", "android", mapOf("en" to "", "my" to ""))
        )
        val data = LocalizationData(locales = listOf("en", "my"), entries = entries)

        AndroidStringsGenerator.generate(data, tempDir, "en")

        val enStrings = File(tempDir, "values/strings.xml").readText()
        assertTrue(enStrings.contains("valid_key"))
        assertTrue(!enStrings.contains("empty_key"))
    }

    @Test
    fun generateCleansExistingOutputDirectory() {
        val tempDir = createTempDir()
        val existingFile = File(tempDir, "old_file.txt")
        existingFile.createNewFile()

        val data = LocalizationData(
            locales = listOf("en"),
            entries = listOf(
                LocalizationEntry("app_name", "android", mapOf("en" to "My App"))
            )
        )

        AndroidStringsGenerator.generate(data, tempDir, "en")

        assertTrue(!existingFile.exists())
    }

    @Test
    fun generateHandlesCommaSeparatedPlatforms() {
        val tempDir = createTempDir()
        val data = LocalizationData(
            locales = listOf("en"),
            entries = listOf(
                LocalizationEntry("key1", "android,ios", mapOf("en" to "Value"))
            )
        )

        AndroidStringsGenerator.generate(data, tempDir, "en")

        val enStrings = File(tempDir, "values/strings.xml").readText()
        assertTrue(enStrings.contains("Value"))
    }

    private fun createTempDir(): File {
        val dir = File.createTempFile("test", "")
        dir.delete()
        dir.mkdirs()
        return dir
    }
}