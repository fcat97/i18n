package io.github.fcat97.i18n.generator

import io.github.fcat97.i18n.parser.LocalizationData
import io.github.fcat97.i18n.parser.LocalizationEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.io.File

class AndroidStringsGeneratorTest {

    @Test
    fun generateCreatesCorrectDirectoryStructure() {
        val tempDir = createTempDir()
        val entries = listOf(
            LocalizationEntry(
                key = "app_name",
                platform = "android",
                defaultValue = "My App",
                translations = mapOf("my" to "My App MM", "zh" to "My App CN")
            )
        )
        val data = LocalizationData(
            defaultLocale = "default",
            additionalLocales = listOf("my", "zh"),
            entries = entries
        )

        AndroidStringsGenerator.generate(data, tempDir)

        assertTrue(File(tempDir, "values").exists())
        assertTrue(File(tempDir, "values-my").exists())
        assertTrue(File(tempDir, "values-zh").exists())
    }

    @Test
    fun generateCreatesValidXmlFiles() {
        val tempDir = createTempDir()
        val entries = listOf(
            LocalizationEntry(
                key = "app_name",
                platform = "android",
                defaultValue = "My App",
                translations = mapOf("my" to "My App MM")
            ),
            LocalizationEntry(
                key = "welcome_text",
                platform = "android",
                defaultValue = "Welcome",
                translations = mapOf("my" to "Welcome MM")
            )
        )
        val data = LocalizationData(
            defaultLocale = "default",
            additionalLocales = listOf("my"),
            entries = entries
        )

        AndroidStringsGenerator.generate(data, tempDir)

        val defaultStrings = File(tempDir, "values/strings.xml").readText()
        assertTrue(defaultStrings.contains("<?xml version=\"1.0\" encoding=\"utf-8\"?>"))
        assertTrue(defaultStrings.contains("<resources>"))
        assertTrue(defaultStrings.contains("</resources>"))
        assertTrue(defaultStrings.contains("name=\"app_name\""))
        assertTrue(defaultStrings.contains("name=\"welcome_text\""))
    }

    @Test
    fun generateFiltersEntriesByPlatform() {
        val tempDir = createTempDir()
        val entries = listOf(
            LocalizationEntry(
                key = "android_key",
                platform = "android",
                defaultValue = "Android Value",
                translations = emptyMap()
            ),
            LocalizationEntry(
                key = "ios_key",
                platform = "ios",
                defaultValue = "iOS Value",
                translations = emptyMap()
            ),
            LocalizationEntry(
                key = "both",
                platform = "android,ios",
                defaultValue = "Both Value",
                translations = emptyMap()
            )
        )
        val data = LocalizationData(
            defaultLocale = "default",
            additionalLocales = emptyList(),
            entries = entries
        )

        AndroidStringsGenerator.generate(data, tempDir)

        val defaultStrings = File(tempDir, "values/strings.xml").readText()
        assertTrue(defaultStrings.contains("Android Value"))
        assertTrue(defaultStrings.contains("Both Value"))
        assertTrue(!defaultStrings.contains("iOS Value"))
    }

    @Test
    fun generateUsesValuesDirectoryForDefaultLocale() {
        val tempDir = createTempDir()
        val entries = listOf(
            LocalizationEntry(
                key = "app_name",
                platform = "android",
                defaultValue = "My App",
                translations = mapOf("my" to "My App MM")
            )
        )
        val data = LocalizationData(
            defaultLocale = "default",
            additionalLocales = listOf("my"),
            entries = entries
        )

        AndroidStringsGenerator.generate(data, tempDir)

        assertTrue(File(tempDir, "values/strings.xml").exists())
    }

    @Test
    fun generateUsesValuesLocaleDirectoryForNonDefaultLocales() {
        val tempDir = createTempDir()
        val entries = listOf(
            LocalizationEntry(
                key = "app_name",
                platform = "android",
                defaultValue = "My App",
                translations = mapOf("my" to "My App MM")
            )
        )
        val data = LocalizationData(
            defaultLocale = "default",
            additionalLocales = listOf("my"),
            entries = entries
        )

        AndroidStringsGenerator.generate(data, tempDir)

        assertTrue(File(tempDir, "values-my/strings.xml").exists())
    }

    @Test
    fun generateEscapesXmlSpecialCharacters() {
        val tempDir = createTempDir()
        val entries = listOf(
            LocalizationEntry(
                key = "ampersand",
                platform = "android",
                defaultValue = "A & B",
                translations = emptyMap()
            ),
            LocalizationEntry(
                key = "less_than",
                platform = "android",
                defaultValue = "A < B",
                translations = emptyMap()
            ),
            LocalizationEntry(
                key = "greater_than",
                platform = "android",
                defaultValue = "A > B",
                translations = emptyMap()
            ),
            LocalizationEntry(
                key = "quote",
                platform = "android",
                defaultValue = "A \"B\"",
                translations = emptyMap()
            ),
            LocalizationEntry(
                key = "apostrophe",
                platform = "android",
                defaultValue = "A 'B'",
                translations = emptyMap()
            )
        )
        val data = LocalizationData(
            defaultLocale = "default",
            additionalLocales = emptyList(),
            entries = entries
        )

        AndroidStringsGenerator.generate(data, tempDir)

        val defaultStrings = File(tempDir, "values/strings.xml").readText()
        assertTrue(defaultStrings.contains("&amp;"))
        assertTrue(defaultStrings.contains("&lt;"))
        assertTrue(defaultStrings.contains("&gt;"))
        assertTrue(defaultStrings.contains("&quot;"))
        assertTrue(defaultStrings.contains("&apos;"))
        assertTrue(!defaultStrings.contains("A & B"))
        assertTrue(!defaultStrings.contains("A < B"))
    }

    @Test
    fun generateSkipsEmptyTranslations() {
        val tempDir = createTempDir()
        val entries = listOf(
            LocalizationEntry(
                key = "valid_key",
                platform = "android",
                defaultValue = "Value",
                translations = mapOf("my" to "")
            ),
            LocalizationEntry(
                key = "empty_key",
                platform = "android",
                defaultValue = "",
                translations = emptyMap()
            )
        )
        val data = LocalizationData(
            defaultLocale = "default",
            additionalLocales = listOf("my"),
            entries = entries
        )

        AndroidStringsGenerator.generate(data, tempDir)

        val defaultStrings = File(tempDir, "values/strings.xml").readText()
        assertTrue(defaultStrings.contains("valid_key"))
        assertTrue(!defaultStrings.contains("empty_key"))
    }

    @Test
    fun generateCleansExistingOutputDirectory() {
        val tempDir = createTempDir()
        val existingFile = File(tempDir, "old_file.txt")
        existingFile.createNewFile()

        val entries = listOf(
            LocalizationEntry(
                key = "app_name",
                platform = "android",
                defaultValue = "My App",
                translations = emptyMap()
            )
        )
        val data = LocalizationData(
            defaultLocale = "default",
            additionalLocales = emptyList(),
            entries = entries
        )

        AndroidStringsGenerator.generate(data, tempDir)

        assertTrue(!existingFile.exists())
    }

    @Test
    fun generateHandlesCommaSeparatedPlatforms() {
        val tempDir = createTempDir()
        val entries = listOf(
            LocalizationEntry(
                key = "key1",
                platform = "android,ios",
                defaultValue = "Value",
                translations = emptyMap()
            )
        )
        val data = LocalizationData(
            defaultLocale = "default",
            additionalLocales = emptyList(),
            entries = entries
        )

        AndroidStringsGenerator.generate(data, tempDir)

        val defaultStrings = File(tempDir, "values/strings.xml").readText()
        assertTrue(defaultStrings.contains("Value"))
    }

    private fun createTempDir(): File {
        val dir = File.createTempFile("test", "")
        dir.delete()
        dir.mkdirs()
        return dir
    }
}