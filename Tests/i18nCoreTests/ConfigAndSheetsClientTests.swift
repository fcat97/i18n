import XCTest
@testable import i18nCore

final class ConfigTests: XCTestCase {

    private var tempDir: String!

    override func setUp() {
        super.setUp()
        tempDir = NSTemporaryDirectory().appending("config-test-\(UUID().uuidString)")
        try? FileManager.default.createDirectory(atPath: tempDir, withIntermediateDirectories: true)
    }

    override func tearDown() {
        try? FileManager.default.removeItem(atPath: tempDir)
        super.tearDown()
    }

    func testLoadValidConfigWithStringsFormat() throws {
        let json = """
        {
            "url": "https://docs.google.com/spreadsheets/d/abc123/edit",
            "outputDir": "MyApp/Resources",
            "format": "strings"
        }
        """
        let path = writeConfig(json)
        let config = try I18nConfig.load(from: path)

        XCTAssertEqual(config.url, "https://docs.google.com/spreadsheets/d/abc123/edit")
        XCTAssertEqual(config.outputDir, "MyApp/Resources")
        XCTAssertEqual(config.format, .strings)
    }

    func testLoadValidConfigWithXcstringsFormat() throws {
        let json = """
        { "url": "https://docs.google.com/spreadsheets/d/xyz/edit", "outputDir": "App/Res", "format": "xcstrings" }
        """
        let config = try I18nConfig.load(from: writeConfig(json))
        XCTAssertEqual(config.format, .xcstrings)
    }

    func testDefaultFormatIsStrings() throws {
        let json = """
        { "url": "https://docs.google.com/spreadsheets/d/xyz/edit", "outputDir": "App/Res" }
        """
        let config = try I18nConfig.load(from: writeConfig(json))
        XCTAssertEqual(config.format, .strings)
    }

    func testFindConfigInSameDirectory() throws {
        let json = """
        { "url": "https://example.com", "outputDir": "out" }
        """
        writeConfig(json, name: "i18n.json")
        let found = I18nConfig.find(in: tempDir)
        XCTAssertNotNil(found)
    }

    func testFindConfigReturnsNilWhenNotFound() {
        let emptyDir = NSTemporaryDirectory().appending("empty-\(UUID().uuidString)")
        try? FileManager.default.createDirectory(atPath: emptyDir, withIntermediateDirectories: true)
        defer { try? FileManager.default.removeItem(atPath: emptyDir) }

        let found = I18nConfig.find(in: emptyDir, rootDirectory: emptyDir)
        XCTAssertNil(found)
    }

    // MARK: - Helpers

    @discardableResult
    private func writeConfig(_ json: String, name: String = "i18n.json") -> String {
        let path = (tempDir as NSString).appendingPathComponent(name)
        try? json.write(toFile: path, atomically: true, encoding: .utf8)
        return path
    }
}

final class SheetsClientTests: XCTestCase {

    func testExtractSheetIdFromFullUrl() {
        let url = "https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgVE2upms/edit"
        XCTAssertEqual(SheetsClient.extractSheetId(from: url), "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgVE2upms")
    }

    func testExtractSheetIdFromUrlWithGid() {
        let url = "https://docs.google.com/spreadsheets/d/abc123def456/edit#gid=0"
        XCTAssertEqual(SheetsClient.extractSheetId(from: url), "abc123def456")
    }

    func testExtractSheetIdReturnsNilForInvalidUrl() {
        XCTAssertNil(SheetsClient.extractSheetId(from: "https://example.com/not-a-sheet"))
    }

    func testCacheFilePathIsDeterministic() {
        let cacheDir = "/tmp/test-cache"
        let url = "https://docs.google.com/spreadsheets/d/abc123/edit"
        let path1 = SheetsClient.cacheFilePath(cacheDir: cacheDir, sheetUrl: url)
        let path2 = SheetsClient.cacheFilePath(cacheDir: cacheDir, sheetUrl: url)
        XCTAssertEqual(path1, path2)
    }

    func testDeleteCacheRemovesFile() throws {
        let cacheDir = NSTemporaryDirectory().appending("cache-test-\(UUID().uuidString)")
        try FileManager.default.createDirectory(atPath: cacheDir, withIntermediateDirectories: true)
        defer { try? FileManager.default.removeItem(atPath: cacheDir) }

        let url = "https://docs.google.com/spreadsheets/d/test123/edit"
        let cacheFile = SheetsClient.cacheFilePath(cacheDir: cacheDir, sheetUrl: url)
        try "cached content".write(toFile: cacheFile, atomically: true, encoding: .utf8)
        XCTAssertTrue(FileManager.default.fileExists(atPath: cacheFile))

        SheetsClient.deleteCache(cacheDir: cacheDir, sheetUrl: url)
        XCTAssertFalse(FileManager.default.fileExists(atPath: cacheFile))
    }
}
