import Foundation

/// Generates a single `Localizable.xcstrings` file (String Catalog format, Xcode 15+).
///
/// Output:
/// ```
/// outputDir/
///   Localizable.xcstrings    ← single JSON file with all locales
/// ```
///
/// File format (JSON):
/// ```json
/// {
///   "version": "1.0",
///   "strings": {
///     "app_name": {
///       "localizations": {
///         "en": { "stringUnit": { "value": "My App", "state": "translated" } }
///       }
///     }
///   }
/// }
/// ```
public enum XcstringsGenerator {
    private static let fileName = "Localizable.xcstrings"
    private static let platform = "ios"

    public static func generate(data: LocalizationData, outputDir: String) throws {
        let fm = FileManager.default
        if fm.fileExists(atPath: outputDir) {
            try fm.removeItem(atPath: outputDir)
        }
        try fm.createDirectory(atPath: outputDir, withIntermediateDirectories: true)

        let iosEntries = data.entries.filter { $0.platform.contains(platform) }

        // Build the strings dictionary
        var stringsDict: [String: Any] = [:]

        for entry in iosEntries {
            var localizations: [String: Any] = [:]

            // Default locale value (stored under the default locale key, e.g. "en" or as sourceLanguage)
            if !entry.defaultValue.isEmpty {
                localizations[data.defaultLocale] = stringUnit(value: entry.defaultValue, state: "translated")
            }

            // Additional locales
            for locale in data.additionalLocales {
                let translated = entry.translations[locale] ?? ""
                let value = translated.isEmpty ? entry.defaultValue : translated
                guard !value.isEmpty else { continue }
                localizations[locale] = stringUnit(value: value, state: "translated")
            }

            guard !localizations.isEmpty else { continue }

            stringsDict[entry.key] = ["localizations": localizations]
        }

        let catalog: [String: Any] = [
            "version": "1.0",
            "sourceLanguage": data.defaultLocale,
            "strings": stringsDict
        ]

        let jsonData = try JSONSerialization.data(withJSONObject: catalog, options: [.prettyPrinted, .sortedKeys])
        let filePath = (outputDir as NSString).appendingPathComponent(fileName)
        try jsonData.write(to: URL(fileURLWithPath: filePath), options: .atomic)
    }

    private static func stringUnit(value: String, state: String) -> [String: Any] {
        ["stringUnit": ["value": value, "state": state]]
    }
}
