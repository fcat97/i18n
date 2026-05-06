// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "i18n",
    platforms: [
        .macOS(.v12)
    ],
    products: [
        .library(name: "i18nCore", targets: ["i18nCore"]),
        .plugin(name: "generate-ios-i18n", targets: ["GenerateI18nPlugin"])
    ],
    targets: [
        // Core library: CSV parsing, sheet download, file generation
        .target(
            name: "i18nCore",
            path: "Sources/i18nCore"
        ),

        // CLI executable invoked by the plugin (runs outside plugin sandbox)
        .executableTarget(
            name: "GenerateI18nCLI",
            dependencies: ["i18nCore"],
            path: "Sources/GenerateI18nCLI"
        ),

        // SPM Command Plugin: wraps the CLI for `swift package generate-ios-i18n`
        .plugin(
            name: "GenerateI18nPlugin",
            capability: .command(
                intent: .custom(
                    verb: "generate-ios-i18n",
                    description: "Generates iOS localization files (Localizable.strings or Localizable.xcstrings) from Google Sheets"
                ),
                permissions: [
                    .writeToPackageDirectory(reason: "Writes generated .strings / .xcstrings localization files into the package directory")
                ]
            ),
            dependencies: ["GenerateI18nCLI"]
        ),

        // Unit tests for the core library
        .testTarget(
            name: "i18nCoreTests",
            dependencies: ["i18nCore"],
            path: "Tests/i18nCoreTests"
        )
    ]
)
