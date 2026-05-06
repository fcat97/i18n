import XCTest
@testable import i18nCore

final class XcstringsGeneratorTests: XCTestCase {

    private var tempDir: String!

    override func setUp() {
        super.setUp()
        tempDir = NSTemporaryDirectory().appending("xcstrings-test-\(UUID().uuidString)")
    }

    override func tearDown() {
        try? FileManager.default.removeItem(atPath: tempDir)
        super.tearDown()
    }

    func testGeneratesSingleXcstringsFile() throws {
        let data = makeData(entries: [makeEntry(key: "app_name", defaultValue: "My App")])
        try XcstringsGenerator.generate(data: data, outputDir: tempDir)

        let path = (tempDir as NSString).appendingPathComponent("Localizable.xcstrings")
        XCTAssertTrue(FileManager.default.fileExists(atPath: path))
    }

    func testXcstringsIsValidJson() throws {
        let data = makeData(entries: [makeEntry(key: "greeting", defaultValue: "Hello")])
        try XcstringsGenerator.generate(data: data, outputDir: tempDir)

        let content = try readXcstrings()
        XCTAssertNotNil(content["version"])
        XCTAssertNotNil(content["strings"])
    }

    func testXcstringsContainsAllLocales() throws {
        let data = makeData(
            entries: [makeEntry(key: "greeting", defaultValue: "Hello", translations: ["en": "Hello", "my": "မင်္ဂလာပါ"])],
            additionalLocales: ["en", "my"]
        )
        try XcstringsGenerator.generate(data: data, outputDir: tempDir)

        let content = try readXcstrings()
        let strings = content["strings"] as? [String: Any]
        let greeting = strings?["greeting"] as? [String: Any]
        let localizations = greeting?["localizations"] as? [String: Any]

        XCTAssertNotNil(localizations?["en"])
        XCTAssertNotNil(localizations?["my"])
    }

    func testXcstringsStringUnitStructure() throws {
        let data = makeData(entries: [makeEntry(key: "key", defaultValue: "Value")])
        try XcstringsGenerator.generate(data: data, outputDir: tempDir)

        let content = try readXcstrings()
        let strings = content["strings"] as? [String: Any]
        let entry = strings?["key"] as? [String: Any]
        let localizations = entry?["localizations"] as? [String: Any]
        let defaultLoc = localizations?["default"] as? [String: Any]
        let stringUnit = defaultLoc?["stringUnit"] as? [String: Any]

        XCTAssertEqual(stringUnit?["value"] as? String, "Value")
        XCTAssertEqual(stringUnit?["state"] as? String, "translated")
    }

    func testXcstringsFiltersAndroidEntries() throws {
        let data = makeData(entries: [
            makeEntry(key: "android_key", platform: "android", defaultValue: "Android"),
            makeEntry(key: "ios_key", platform: "ios", defaultValue: "iOS"),
            makeEntry(key: "both_key", platform: "android,ios", defaultValue: "Both")
        ])
        try XcstringsGenerator.generate(data: data, outputDir: tempDir)

        let content = try readXcstrings()
        let strings = content["strings"] as? [String: Any]

        XCTAssertNotNil(strings?["ios_key"])
        XCTAssertNotNil(strings?["both_key"])
        XCTAssertNil(strings?["android_key"])
    }

    func testXcstringsSkipsEmptyValues() throws {
        let data = makeData(entries: [
            makeEntry(key: "valid", defaultValue: "Value"),
            makeEntry(key: "empty", defaultValue: "")
        ])
        try XcstringsGenerator.generate(data: data, outputDir: tempDir)

        let content = try readXcstrings()
        let strings = content["strings"] as? [String: Any]

        XCTAssertNotNil(strings?["valid"])
        XCTAssertNil(strings?["empty"])
    }

    func testXcstringsFallsBackToDefaultWhenTranslationEmpty() throws {
        let data = makeData(
            entries: [makeEntry(key: "key", defaultValue: "Default", translations: ["fr": ""])],
            additionalLocales: ["fr"]
        )
        try XcstringsGenerator.generate(data: data, outputDir: tempDir)

        let content = try readXcstrings()
        let strings = content["strings"] as? [String: Any]
        let entry = strings?["key"] as? [String: Any]
        let localizations = entry?["localizations"] as? [String: Any]
        let frLoc = localizations?["fr"] as? [String: Any]
        let stringUnit = frLoc?["stringUnit"] as? [String: Any]

        XCTAssertEqual(stringUnit?["value"] as? String, "Default")
    }

    func testXcstringsCleansOutputDir() throws {
        try FileManager.default.createDirectory(atPath: tempDir, withIntermediateDirectories: true)
        let oldFile = (tempDir as NSString).appendingPathComponent("old.txt")
        FileManager.default.createFile(atPath: oldFile, contents: nil)

        let data = makeData(entries: [makeEntry(key: "k", defaultValue: "v")])
        try XcstringsGenerator.generate(data: data, outputDir: tempDir)

        XCTAssertFalse(FileManager.default.fileExists(atPath: oldFile))
    }

    func testXcstringsVersion() throws {
        let data = makeData(entries: [makeEntry(key: "k", defaultValue: "v")])
        try XcstringsGenerator.generate(data: data, outputDir: tempDir)

        let content = try readXcstrings()
        XCTAssertEqual(content["version"] as? String, "1.0")
    }

    // MARK: - Helpers

    private func makeData(
        entries: [LocalizationEntry],
        additionalLocales: [String] = []
    ) -> LocalizationData {
        LocalizationData(defaultLocale: "default", additionalLocales: additionalLocales, entries: entries)
    }

    private func makeEntry(
        key: String,
        platform: String = "ios",
        defaultValue: String,
        translations: [String: String] = [:]
    ) -> LocalizationEntry {
        LocalizationEntry(key: key, platform: platform, defaultValue: defaultValue, translations: translations)
    }

    private func readXcstrings() throws -> [String: Any] {
        let path = (tempDir as NSString).appendingPathComponent("Localizable.xcstrings")
        let data = try Data(contentsOf: URL(fileURLWithPath: path))
        let json = try JSONSerialization.jsonObject(with: data)
        return json as! [String: Any]
    }
}
