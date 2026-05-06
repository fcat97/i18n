package io.github.fcat97.i18n.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ExcelParserTest {

    @Test
    fun parseValidCsv() {
        val csv = """key,platform,default,en,my,zh
app_name,android,My App,My App English,My App Myanmar,My App Chinese
welcome_text,android,Welcome,Welcome English,Welcome Myanmar,Welcome Chinese
button_text,android,Click Me,Click Me English,Click Me Myanmar,Click Me Chinese"""

        val result = ExcelParser.parse(csv)

        assertEquals("default", result.defaultLocale)
        assertEquals(3, result.additionalLocales.size)
        assertTrue("en" in result.additionalLocales)
        assertTrue("my" in result.additionalLocales)
        assertTrue("zh" in result.additionalLocales)

        assertEquals(3, result.entries.size)

        val appNameEntry = result.entries.find { it.key == "app_name" }
        assertEquals("android", appNameEntry?.platform)
        assertEquals("My App", appNameEntry?.defaultValue)
        assertEquals("My App English", appNameEntry?.translations?.get("en"))
        assertEquals("My App Myanmar", appNameEntry?.translations?.get("my"))
        assertEquals("My App Chinese", appNameEntry?.translations?.get("zh"))
    }

    @Test
    fun parseEmptyCsv() {
        val csv = ""

        val result = ExcelParser.parse(csv)

        assertEquals("default", result.defaultLocale)
        assertTrue(result.additionalLocales.isEmpty())
        assertTrue(result.entries.isEmpty())
    }

    @Test
    fun parseValidatesRequiredHeaders() {
        val csv = "other_column,en"
        assertFailsWith<IllegalArgumentException> {
            ExcelParser.parse(csv)
        }
    }

    @Test
    fun parseSkipsEmptyKeyRows() {
        val csv = """key,platform,default,en,my
,android,Value0,Value1,Value2
valid_key,android,Value3,Value4,Value5"""

        val result = ExcelParser.parse(csv)

        assertEquals(1, result.entries.size)
        assertEquals("valid_key", result.entries[0].key)
    }

    @Test
    fun parseHandlesMultiplePlatforms() {
        val csv = """key,platform,default,en,my
app_name,android,My App,My App English,My App Myanmar
hello_text,ios,Hello,Hello English,Hello Myanmar"""

        val result = ExcelParser.parse(csv)

        val androidEntries = result.entries.filter { it.platform.contains("android") }
        val iosEntries = result.entries.filter { it.platform.contains("ios") }

        assertEquals(1, androidEntries.size)
        assertEquals(1, iosEntries.size)
    }

    @Test
    fun parseHandlesMissingTranslationsAsEmpty() {
        val csv = """key,platform,default,en,my,zh
app_name,android,My App,, """

        val result = ExcelParser.parse(csv)

        val entry = result.entries[0]
        assertEquals("My App", entry.defaultValue)
        assertEquals("", entry.translations["en"])
        assertEquals("", entry.translations["my"])
        assertTrue("zh" !in entry.translations)
    }

    @Test
    fun parseTrimsWhitespaceFromValues() {
        val csv = """key,platform,default,en,my
app_name,android,My App, My App English , My App Myanmar """

        val result = ExcelParser.parse(csv)

        assertEquals("My App English", result.entries[0].translations["en"])
        assertEquals("My App Myanmar", result.entries[0].translations["my"])
    }

    @Test
    fun parseHandlesCsvWithOnlyHeaderRow() {
        val csv = "key,platform,default,en"

        val result = ExcelParser.parse(csv)

        assertEquals("default", result.defaultLocale)
        assertEquals(1, result.additionalLocales.size)
        assertEquals("en", result.additionalLocales[0])
        assertTrue(result.entries.isEmpty())
    }
}
