import Foundation

// MARK: - Output Format

public enum OutputFormat: String, Codable {
    /// Classic per-locale `.lproj/Localizable.strings` files. Compatible with all iOS versions.
    case strings
    /// Modern single `Localizable.xcstrings` JSON file. Requires Xcode 15+ / iOS 17+.
    case xcstrings
}

// MARK: - Localization Models

public struct LocalizationEntry {
    public let key: String
    public let platform: String
    public let defaultValue: String
    public let translations: [String: String]

    public init(key: String, platform: String, defaultValue: String, translations: [String: String]) {
        self.key = key
        self.platform = platform
        self.defaultValue = defaultValue
        self.translations = translations
    }
}

public struct LocalizationData {
    public let defaultLocale: String
    public let additionalLocales: [String]
    public let entries: [LocalizationEntry]

    public init(defaultLocale: String, additionalLocales: [String], entries: [LocalizationEntry]) {
        self.defaultLocale = defaultLocale
        self.additionalLocales = additionalLocales
        self.entries = entries
    }
}
