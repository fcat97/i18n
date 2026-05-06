import XCTest
@testable import i18nCore

final class ExcelParserTests: XCTestCase {

    func testParseValidCsv() throws {
        let csv = """
key,platform,default,en,my,zh
app_name,ios,My App,My App English,My App Myanmar,My App Chinese
welcome_text,ios,Welcome,Welcome English,Welcome Myanmar,Welcome Chinese
"""
        let result = try ExcelParser.parse(csv)

        XCTAssertEqual(result.defaultLocale, "default")
        XCTAssertEqual(result.additionalLocales.count, 3)
        XCTAssertTrue(result.additionalLocales.contains("en"))
        XCTAssertTrue(result.additionalLocales.contains("my"))
        XCTAssertTrue(result.additionalLocales.contains("zh"))
        XCTAssertEqual(result.entries.count, 2)

        let appName = result.entries.first { $0.key == "app_name" }
        XCTAssertNotNil(appName)
        XCTAssertEqual(appName?.platform, "ios")
        XCTAssertEqual(appName?.defaultValue, "My App")
        XCTAssertEqual(appName?.translations["en"], "My App English")
        XCTAssertEqual(appName?.translations["my"], "My App Myanmar")
    }

    func testParseEmptyCsv() throws {
        let result = try ExcelParser.parse("")
        XCTAssertEqual(result.defaultLocale, "default")
        XCTAssertTrue(result.additionalLocales.isEmpty)
        XCTAssertTrue(result.entries.isEmpty)
    }

    func testParseMissingRequiredHeaderThrows() {
        let csv = "other_column,en"
        XCTAssertThrowsError(try ExcelParser.parse(csv))
    }

    func testParseSkipsEmptyKeyRows() throws {
        let csv = """
key,platform,default,en
,ios,Value0,Value1
valid_key,ios,Value2,Value3
"""
        let result = try ExcelParser.parse(csv)
        XCTAssertEqual(result.entries.count, 1)
        XCTAssertEqual(result.entries[0].key, "valid_key")
    }

    func testParseHandlesMultiplePlatforms() throws {
        let csv = """
key,platform,default,en
app_name,android,My App,My App English
hello_text,ios,Hello,Hello English
shared,android\\,ios,Shared,Shared English
"""
        let result = try ExcelParser.parse(csv)
        let iosEntries = result.entries.filter { $0.platform.contains("ios") }
        XCTAssertTrue(iosEntries.count >= 1)
    }

    func testParseTrimsWhitespace() throws {
        let csv = """
key,platform,default,en,my
app_name,ios,My App, My App English , My App Myanmar 
"""
        let result = try ExcelParser.parse(csv)
        XCTAssertEqual(result.entries[0].translations["en"], "My App English")
        XCTAssertEqual(result.entries[0].translations["my"], "My App Myanmar")
    }

    func testParseHeaderOnlyRow() throws {
        let csv = "key,platform,default,en"
        let result = try ExcelParser.parse(csv)
        XCTAssertEqual(result.additionalLocales, ["en"])
        XCTAssertTrue(result.entries.isEmpty)
    }

    func testParseQuotedValues() throws {
        let csv = """
key,platform,default,en
msg,ios,"Hello, World","English, Hello"
"""
        let result = try ExcelParser.parse(csv)
        XCTAssertEqual(result.entries[0].defaultValue, "Hello, World")
        XCTAssertEqual(result.entries[0].translations["en"], "English, Hello")
    }
}
