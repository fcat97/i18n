import Foundation

/// Parses CSV content downloaded from Google Sheets into `LocalizationData`.
///
/// Expected column layout:
/// | key | platform | default | en | my | zh | ... |
public enum ExcelParser {
    private static let requiredHeaders = ["key", "platform"]
    private static let defaultColumn = "default"

    public static func parse(_ csvContent: String) throws -> LocalizationData {
        let lines = csvContent.components(separatedBy: .newlines).filter { !$0.trimmingCharacters(in: .whitespaces).isEmpty }

        guard !lines.isEmpty else {
            return LocalizationData(defaultLocale: defaultColumn, additionalLocales: [], entries: [])
        }

        let headers = parseCsvLine(lines[0])
        try validateHeaders(headers)

        let defaultIndex = headers.firstIndex(of: defaultColumn) ?? -1
        let additionalLocales = headers.filter { $0 != defaultColumn && !requiredHeaders.contains($0) }

        let entries = lines.dropFirst().compactMap { line -> LocalizationEntry? in
            parseEntry(parseCsvLine(line), defaultIndex: defaultIndex, additionalLocales: additionalLocales)
        }

        return LocalizationData(defaultLocale: defaultColumn, additionalLocales: additionalLocales, entries: entries)
    }

    private static func validateHeaders(_ headers: [String]) throws {
        for required in requiredHeaders {
            guard headers.contains(required) else {
                throw I18nError.missingHeader(required)
            }
        }
        guard headers.contains(defaultColumn) else {
            throw I18nError.missingHeader(defaultColumn)
        }
    }

    private static func parseCsvLine(_ line: String) -> [String] {
        var result: [String] = []
        var remaining = line[line.startIndex...]

        while !remaining.isEmpty {
            if remaining.first == "\"" {
                remaining = remaining.dropFirst()
                if let endQuote = remaining.firstIndex(of: "\"") {
                    result.append(String(remaining[..<endQuote]))
                    remaining = remaining[remaining.index(after: endQuote)...]
                    if remaining.first == "," { remaining = remaining.dropFirst() }
                } else {
                    result.append(String(remaining))
                    remaining = remaining[remaining.endIndex...]
                }
            } else if let comma = remaining.firstIndex(of: ",") {
                result.append(String(remaining[..<comma]))
                remaining = remaining[remaining.index(after: comma)...]
            } else {
                result.append(String(remaining))
                remaining = remaining[remaining.endIndex...]
            }
        }
        return result
    }

    private static func parseEntry(_ values: [String], defaultIndex: Int, additionalLocales: [String]) -> LocalizationEntry? {
        guard !values.isEmpty else { return nil }

        let key = values[0].trimmingCharacters(in: .whitespaces)
        guard !key.isEmpty else { return nil }

        let platform = values.count > 1 ? values[1].trimmingCharacters(in: .whitespaces).lowercased() : ""
        let defaultValue = defaultIndex >= 0 && defaultIndex < values.count
            ? values[defaultIndex].trimmingCharacters(in: .whitespaces)
            : ""

        var translations: [String: String] = [:]
        for (localeIndex, locale) in additionalLocales.enumerated() {
            let valueIndex = 3 + localeIndex
            if valueIndex < values.count {
                translations[locale] = values[valueIndex].trimmingCharacters(in: .whitespaces)
            }
        }

        return LocalizationEntry(key: key, platform: platform, defaultValue: defaultValue, translations: translations)
    }
}

public enum I18nError: Error, LocalizedError {
    case missingHeader(String)
    case invalidSheetUrl(String)
    case downloadFailed(String)
    case configNotFound
    case configInvalid(String)

    public var errorDescription: String? {
        switch self {
        case .missingHeader(let h): return "Missing required CSV header: \(h)"
        case .invalidSheetUrl(let u): return "Invalid Google Sheet URL: \(u)"
        case .downloadFailed(let msg): return "Failed to download sheet: \(msg)"
        case .configNotFound: return "i18n.json not found. Create one in your project root."
        case .configInvalid(let msg): return "i18n.json is invalid: \(msg)"
        }
    }
}
