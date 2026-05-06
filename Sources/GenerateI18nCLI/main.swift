import Foundation
import i18nCore

// MARK: - CLI Entry Point
// Invoked by the SPM Command Plugin with the package directory as the first argument.
// Usage: GenerateI18nCLI <packageDirectory>

let args = CommandLine.arguments
let packageDir = args.count > 1 ? args[1] : FileManager.default.currentDirectoryPath

print("[i18n] Starting iOS localization generation")
print("[i18n] Package directory: \(packageDir)")

do {
    // 1. Load config
    guard let configPath = I18nConfig.find(in: packageDir) else {
        throw I18nError.configNotFound
    }
    print("[i18n] Config: \(configPath)")
    let config = try I18nConfig.load(from: configPath)
    print("[i18n] Format: \(config.format.rawValue)")

    // Resolve output directory relative to config file location
    let configDir = (configPath as NSString).deletingLastPathComponent
    let outputDir = (configDir as NSString).appendingPathComponent(config.outputDir)
    print("[i18n] Output: \(outputDir)")

    // 2. Download sheet
    let cacheDir = (packageDir as NSString).appendingPathComponent(".i18n-cache")
    print("[i18n] Downloading sheet from: \(config.url)")
    let csv: String
    do {
        csv = try SheetsClient.downloadWithCache(sheetUrl: config.url, cacheDir: cacheDir)
    } catch {
        SheetsClient.deleteCache(cacheDir: cacheDir, sheetUrl: config.url)
        throw error
    }
    print("[i18n] Downloaded \(csv.components(separatedBy: .newlines).count) rows")

    // 3. Parse CSV
    let data = try ExcelParser.parse(csv)
    print("[i18n] Default locale: \(data.defaultLocale)")
    print("[i18n] Additional locales: \(data.additionalLocales.joined(separator: ", "))")
    print("[i18n] Keys found: \(data.entries.count)")

    // 4. Generate files
    switch config.format {
    case .strings:
        print("[i18n] Generating .lproj/Localizable.strings files...")
        try IosStringsGenerator.generate(data: data, outputDir: outputDir)
        let fileCount = 1 + data.additionalLocales.count
        print("[i18n] Generated \(fileCount) Localizable.strings file(s)")

    case .xcstrings:
        print("[i18n] Generating Localizable.xcstrings...")
        try XcstringsGenerator.generate(data: data, outputDir: outputDir)
        print("[i18n] Generated Localizable.xcstrings")
    }

    print("[i18n] Done! Files written to: \(outputDir)")

} catch let error as I18nError {
    fputs("[i18n] Error: \(error.errorDescription ?? error.localizedDescription)\n", stderr)
    exit(1)
} catch {
    fputs("[i18n] Error: \(error.localizedDescription)\n", stderr)
    exit(1)
}
