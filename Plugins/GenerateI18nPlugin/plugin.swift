import PackagePlugin
import Foundation

@main
struct GenerateI18nPlugin: CommandPlugin {

    func performCommand(context: PluginContext, arguments: [String]) async throws {
        let packageDir = context.package.directory.string

        // Locate the built CLI executable (built as a dependency of this plugin)
        let tool = try context.tool(named: "GenerateI18nCLI")

        // Run the CLI, passing the package directory so it can find i18n.json
        let process = Process()
        process.executableURL = URL(fileURLWithPath: tool.path.string)
        process.arguments = [packageDir]
        process.standardOutput = FileHandle.standardOutput
        process.standardError = FileHandle.standardError

        try process.run()
        process.waitUntilExit()

        guard process.terminationStatus == 0 else {
            throw GenerateI18nError.executionFailed(code: process.terminationStatus)
        }
    }
}

enum GenerateI18nError: Error, LocalizedError {
    case executionFailed(code: Int32)

    var errorDescription: String? {
        switch self {
        case .executionFailed(let code):
            return "GenerateI18nCLI exited with code \(code)"
        }
    }
}
