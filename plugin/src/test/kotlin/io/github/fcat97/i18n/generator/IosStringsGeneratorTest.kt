package io.github.fcat97.i18n.generator

import io.github.fcat97.i18n.parser.LocalizationData
import io.github.fcat97.i18n.parser.LocalizationEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.io.File

class IosStringsGeneratorTest {

    @Test
    fun generateCreatesCorrectDirectoryStructure() {
        val tempDir = createTempDir()
        val entries = listOf(
            LocalizationEntry(
                key = "app_name",
                platform = "ios",
                defaultValue = "My App",
                translations = mapOf("my" to "My App MM", "zh" to "My App CN")
            )
        )
        val data = LocalizationData(
            defaultLocale = "default",
            additionalLocales = listOf("my", "zh"),
            entries = entries
        )

        IosStringsGenerator.generate(data, tempDir)

        assertTrue(File(tempDir, "Base.lproj").exists())
        assertTrue(File(tempDir, "my.lproj").exists())
        assertTrue(File(tempDir, "zh.lproj").exists())
    }

    @Test
    fun generateCreatesLocalizableStringsFiles() {
        val tempDir = createTempDir()
        val entries = listOf(
            LocalizationEntry(
                key = "app_name",
                platform = "ios",
                defaultValue = "My App",
                translations = mapOf("my" to "My App MM")
            )
        )
        val data = LocalizationData(
            defaultLocale = "default",
            additionalLocales = listOf("my"),
            entries = entries
        )

        IosStringsGenerator.generate(data, tempDir)

        assertTrue(File(tempDir, "Base.lproj/Localizable.strings").exists())
        assertTrue(File(tempDir, "my.lproj/Localizable.strings").exists())
    }

    @Test
    fun generateCreatesValidStringsFormat() {
        val tempDir = createTempDir()
        val entries = listOf(
            LocalizationEntry(
                key = "app_name",
                platform = "ios",
                defaultValue = "My App",
                translations = mapOf("my" to "My App Myanmar")
            )
        )
        val data = LocalizationData(
            defaultLocale = "default",
            additionalLocales = listOf("my"),
            entries = entries
        )

        IosStringsGenerator.generate(data, tempDir)

        val baseStrings = File(tempDir, "Base.lproj/Localizable.strings").readText()
        assertTrue(baseStrings.contains("\"app_name\" = \"My App\";"))

        val myStrings = File(tempDir, "my.lproj/Localizable.strings").readText()
        assertTrue(myStrings.contains("\"app_name\" = \"My App Myanmar\";"))
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
                key = "both_key",
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

        IosStringsGenerator.generate(data, tempDir)

        val baseStrings = File(tempDir, "Base.lproj/Localizable.strings").readText()
        assertTrue(baseStrings.contains("iOS Value"))
        assertTrue(baseStrings.contains("Both Value"))
        assertTrue(!baseStrings.contains("Android Value"))
    }

    @Test
    fun generateUsesDefaultValueWhenTranslationMissing() {
        val tempDir = createTempDir()
        val entries = listOf(
            LocalizationEntry(
                key = "app_name",
                platform = "ios",
                defaultValue = "My App",
                translations = emptyMap()
            )
        )
        val data = LocalizationData(
            defaultLocale = "default",
            additionalLocales = listOf("en"),
            entries = entries
        )

        IosStringsGenerator.generate(data, tempDir)

        val enStrings = File(tempDir, "en.lproj/Localizable.strings").readText()
        assertTrue(enStrings.contains("\"app_name\" = \"My App\";"))
    }

    @Test
    fun generateEscapesDoubleQuotes() {
        val tempDir = createTempDir()
        val entries = listOf(
            LocalizationEntry(
                key = "quote_key",
                platform = "ios",
                defaultValue = "Say \"Hello\"",
                translations = emptyMap()
            )
        )
        val data = LocalizationData(
            defaultLocale = "default",
            additionalLocales = emptyList(),
            entries = entries
        )

        IosStringsGenerator.generate(data, tempDir)

        val baseStrings = File(tempDir, "Base.lproj/Localizable.strings").readText()
        assertTrue(baseStrings.contains("\"quote_key\" = \"Say \\\"Hello\\\"\";"))
    }

    @Test
    fun generateEscapesBackslashes() {
        val tempDir = createTempDir()
        val entries = listOf(
            LocalizationEntry(
                key = "path_key",
                platform = "ios",
                defaultValue = "C:\\Users\\test",
                translations = emptyMap()
            )
        )
        val data = LocalizationData(
            defaultLocale = "default",
            additionalLocales = emptyList(),
            entries = entries
        )

        IosStringsGenerator.generate(data, tempDir)

        val baseStrings = File(tempDir, "Base.lproj/Localizable.strings").readText()
        assertTrue(baseStrings.contains("C:\\\\Users\\\\test"))
    }

    @Test
    fun generateEscapesNewlines() {
        val tempDir = createTempDir()
        val entries = listOf(
            LocalizationEntry(
                key = "multiline",
                platform = "ios",
                defaultValue = "Line1\nLine2",
                translations = emptyMap()
            )
        )
        val data = LocalizationData(
            defaultLocale = "default",
            additionalLocales = emptyList(),
            entries = entries
        )

        IosStringsGenerator.generate(data, tempDir)

        val baseStrings = File(tempDir, "Base.lproj/Localizable.strings").readText()
        assertTrue(baseStrings.contains("\"multiline\" = \"Line1\\nLine2\";"))
    }

    @Test
    fun generateSkipsEmptyValues() {
        val tempDir = createTempDir()
        val entries = listOf(
            LocalizationEntry(
                key = "valid_key",
                platform = "ios",
                defaultValue = "Valid",
                translations = emptyMap()
            ),
            LocalizationEntry(
                key = "empty_key",
                platform = "ios",
                defaultValue = "",
                translations = emptyMap()
            )
        )
        val data = LocalizationData(
            defaultLocale = "default",
            additionalLocales = emptyList(),
            entries = entries
        )

        IosStringsGenerator.generate(data, tempDir)

        val baseStrings = File(tempDir, "Base.lproj/Localizable.strings").readText()
        assertTrue(baseStrings.contains("valid_key"))
        assertTrue(!baseStrings.contains("empty_key"))
    }

    @Test
    fun generateCleansExistingOutputDirectory() {
        val tempDir = createTempDir()
        val existingFile = File(tempDir, "old_file.txt")
        existingFile.createNewFile()

        val entries = listOf(
            LocalizationEntry(
                key = "app_name",
                platform = "ios",
                defaultValue = "My App",
                translations = emptyMap()
            )
        )
        val data = LocalizationData(
            defaultLocale = "default",
            additionalLocales = emptyList(),
            entries = entries
        )

        IosStringsGenerator.generate(data, tempDir)

        assertTrue(!existingFile.exists())
    }

    @Test
    fun generateHandlesCommaSeparatedPlatforms() {
        val tempDir = createTempDir()
        val entries = listOf(
            LocalizationEntry(
                key = "shared_key",
                platform = "android,ios",
                defaultValue = "Shared Value",
                translations = emptyMap()
            )
        )
        val data = LocalizationData(
            defaultLocale = "default",
            additionalLocales = emptyList(),
            entries = entries
        )

        IosStringsGenerator.generate(data, tempDir)

        val baseStrings = File(tempDir, "Base.lproj/Localizable.strings").readText()
        assertTrue(baseStrings.contains("Shared Value"))
    }

    @Test
    fun generateIncludesAutoGeneratedComment() {
        val tempDir = createTempDir()
        val entries = listOf(
            LocalizationEntry(
                key = "app_name",
                platform = "ios",
                defaultValue = "My App",
                translations = emptyMap()
            )
        )
        val data = LocalizationData(
            defaultLocale = "default",
            additionalLocales = emptyList(),
            entries = entries
        )

        IosStringsGenerator.generate(data, tempDir)

        val baseStrings = File(tempDir, "Base.lproj/Localizable.strings").readText()
        assertTrue(baseStrings.startsWith("/* Auto-generated"))
    }

    @Test
    fun generateMultipleLocales() {
        val tempDir = createTempDir()
        val entries = listOf(
            LocalizationEntry(
                key = "greeting",
                platform = "ios",
                defaultValue = "Hello",
                translations = mapOf("my" to "မင်္ဂလာပါ", "zh" to "你好", "es" to "Hola")
            )
        )
        val data = LocalizationData(
            defaultLocale = "default",
            additionalLocales = listOf("my", "zh", "es"),
            entries = entries
        )

        IosStringsGenerator.generate(data, tempDir)

        assertTrue(File(tempDir, "Base.lproj/Localizable.strings").readText().contains("\"greeting\" = \"Hello\";"))
        assertTrue(File(tempDir, "my.lproj/Localizable.strings").readText().contains("မင်္ဂလာပါ"))
        assertTrue(File(tempDir, "zh.lproj/Localizable.strings").readText().contains("你好"))
        assertTrue(File(tempDir, "es.lproj/Localizable.strings").readText().contains("\"greeting\" = \"Hola\";"))
    }

    @Test
    fun escapeStringsHandlesAllSpecialChars() {
        assertEquals("hello", IosStringsGenerator.escapeStrings("hello"))
        assertEquals("say \\\"hi\\\"", IosStringsGenerator.escapeStrings("say \"hi\""))
        assertEquals("back\\\\slash", IosStringsGenerator.escapeStrings("back\\slash"))
        assertEquals("line1\\nline2", IosStringsGenerator.escapeStrings("line1\nline2"))
        assertEquals("no\\\\n\\\"quotes\\\"", IosStringsGenerator.escapeStrings("no\\n\"quotes\""))
    }

    private fun createTempDir(): File {
        val dir = File.createTempFile("ios-test", "")
        dir.delete()
        dir.mkdirs()
        return dir
    }
}
