import Foundation

/// Configuration loaded from `i18n.json` in the project root.
public struct I18nConfig: Codable {
    /// Google Sheets URL (e.g., `https://docs.google.com/spreadsheets/d/SHEET_ID/edit`)
    public let url: String

    /// Directory where localization files will be written (relative to `i18n.json` location).
    public let outputDir: String

    /// Output format: `"strings"` (default) or `"xcstrings"`.
    public let format: OutputFormat

    public init(url: String, outputDir: String, format: OutputFormat = .strings) {
        self.url = url
        self.outputDir = outputDir
        self.format = format
    }

    private enum CodingKeys: String, CodingKey {
        case url, outputDir, format
    }

    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        url = try container.decode(String.self, forKey: .url)
        outputDir = try container.decode(String.self, forKey: .outputDir)
        format = try container.decodeIfPresent(OutputFormat.self, forKey: .format) ?? .strings
    }

    /// Load config from a `i18n.json` file at the given path.
    public static func load(from filePath: String) throws -> I18nConfig {
        let url = URL(fileURLWithPath: filePath)
        let data = try Data(contentsOf: url)
        let decoder = JSONDecoder()
        return try decoder.decode(I18nConfig.self, from: data)
    }

    /// Search for `i18n.json` starting at `directory` and walking up to `rootDirectory`.
    public static func find(in directory: String, rootDirectory: String? = nil) -> String? {
        var current = URL(fileURLWithPath: directory)
        let root = rootDirectory.map { URL(fileURLWithPath: $0) }

        while true {
            let candidate = current.appendingPathComponent("i18n.json")
            if FileManager.default.fileExists(atPath: candidate.path) {
                return candidate.path
            }
            let parent = current.deletingLastPathComponent()
            if parent.path == current.path { break }
            if let root = root, current.path == root.path { break }
            current = parent
        }
        return nil
    }
}
