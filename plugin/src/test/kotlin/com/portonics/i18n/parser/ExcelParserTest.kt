package com.portonics.i18n.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ExcelParserTest {

    @Test
    fun parseValidCsv() {
        val csv = """key,platform,en,my,zh
app_name,android,My App,My App Myanmar,My App Chinese
welcome_text,android,Welcome,Welcome Myanmar,Welcome Chinese
button_text,android,Click Me,Click Me Myanmar,Click Me Chinese"""

        val result = ExcelParser.parse(csv)

        assertEquals(3, result.locales.size)
        assertTrue("en" in result.locales)
        assertTrue("my" in result.locales)
        assertTrue("zh" in result.locales)

        assertEquals(3, result.entries.size)

        val appNameEntry = result.entries.find { it.key == "app_name" }
        assertEquals("android", appNameEntry?.platform)
        assertEquals("My App", appNameEntry?.translations?.get("en"))
        assertEquals("My App Myanmar", appNameEntry?.translations?.get("my"))
        assertEquals("My App Chinese", appNameEntry?.translations?.get("zh"))
    }

    @Test
    fun parseEmptyCsv() {
        val csv = ""

        val result = ExcelParser.parse(csv)

        assertTrue(result.locales.isEmpty())
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
        val csv = """key,platform,en,my
,android,Value1,Value2
valid_key,android,Value3,Value4"""

        val result = ExcelParser.parse(csv)

        assertEquals(1, result.entries.size)
        assertEquals("valid_key", result.entries[0].key)
    }

    @Test
    fun parseHandlesMultiplePlatforms() {
        val csv = """key,platform,en,my
app_name,android,My App,My App Myanmar
hello_text,ios,Hello,Hello Myanmar"""

        val result = ExcelParser.parse(csv)

        val androidEntries = result.entries.filter { it.platform.contains("android") }
        val iosEntries = result.entries.filter { it.platform.contains("ios") }

        assertEquals(1, androidEntries.size)
        assertEquals(1, iosEntries.size)
    }

    @Test
    fun parseHandlesMissingTranslationsAsEmpty() {
        val csv = """key,platform,en,my,zh
app_name,android,My App,, """

        val result = ExcelParser.parse(csv)

        val entry = result.entries[0]
        assertEquals("My App", entry.translations["en"])
        assertEquals("", entry.translations["my"])
        assertEquals("", entry.translations["zh"])
    }

    @Test
    fun parseTrimsWhitespaceFromValues() {
        val csv = """key,platform,en,my
app_name,android, My App , My App Myanmar """

        val result = ExcelParser.parse(csv)

        assertEquals("My App", result.entries[0].translations["en"])
        assertEquals("My App Myanmar", result.entries[0].translations["my"])
    }

    @Test
    fun parseHandlesCsvWithOnlyHeaderRow() {
        val csv = "key,platform,en"

        val result = ExcelParser.parse(csv)

        assertEquals(1, result.locales.size)
        assertEquals("en", result.locales[0])
        assertTrue(result.entries.isEmpty())
    }
}