import Foundation
import CryptoKit

/// Downloads CSV data from a public Google Sheet URL with optional file-based caching.
public enum SheetsClient {

    // MARK: - Public API

    /// Download CSV from the given Google Sheets URL.
    /// Uses cache if available; falls back to network download.
    public static func downloadWithCache(sheetUrl: String, cacheDir: String) throws -> String {
        let cacheFile = cacheFilePath(cacheDir: cacheDir, sheetUrl: sheetUrl)

        if FileManager.default.fileExists(atPath: cacheFile) {
            return try String(contentsOfFile: cacheFile, encoding: .utf8)
        }

        let content = try downloadPublicSheet(sheetUrl: sheetUrl)
        try? FileManager.default.createDirectory(atPath: cacheDir, withIntermediateDirectories: true)
        try content.write(toFile: cacheFile, atomically: true, encoding: .utf8)
        return content
    }

    /// Delete the cached file for a given sheet URL (called on failure to force re-download).
    public static func deleteCache(cacheDir: String, sheetUrl: String) {
        let cacheFile = cacheFilePath(cacheDir: cacheDir, sheetUrl: sheetUrl)
        try? FileManager.default.removeItem(atPath: cacheFile)
    }

    /// Download CSV from a public Google Sheet without using cache.
    public static func downloadPublicSheet(sheetUrl: String) throws -> String {
        guard let sheetId = extractSheetId(from: sheetUrl) else {
            throw I18nError.invalidSheetUrl(sheetUrl)
        }

        let exportUrl = "https://docs.google.com/spreadsheets/d/\(sheetId)/export?format=csv"
        guard let url = URL(string: exportUrl) else {
            throw I18nError.invalidSheetUrl(sheetUrl)
        }

        return try synchronousDownload(url: url)
    }

    // MARK: - URL Helpers

    public static func extractSheetId(from url: String) -> String? {
        let pattern = #"/spreadsheets/d/([a-zA-Z0-9\-_]+)"#
        guard let regex = try? NSRegularExpression(pattern: pattern),
              let match = regex.firstMatch(in: url, range: NSRange(url.startIndex..., in: url)),
              let range = Range(match.range(at: 1), in: url) else {
            return nil
        }
        return String(url[range])
    }

    public static func cacheFilePath(cacheDir: String, sheetUrl: String) -> String {
        let hash = md5(sheetUrl)
        return (cacheDir as NSString).appendingPathComponent("\(hash).csv")
    }

    // MARK: - Private Helpers

    private static func synchronousDownload(url: URL) throws -> String {
        var result: Result<String, Error> = .failure(I18nError.downloadFailed("Timeout"))
        let semaphore = DispatchSemaphore(value: 0)

        var request = URLRequest(url: url)
        request.setValue("Mozilla/5.0", forHTTPHeaderField: "User-Agent")

        URLSession.shared.dataTask(with: request) { data, response, error in
            defer { semaphore.signal() }
            if let error = error {
                result = .failure(I18nError.downloadFailed(error.localizedDescription))
                return
            }
            guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
                let code = (response as? HTTPURLResponse)?.statusCode ?? 0
                result = .failure(I18nError.downloadFailed("HTTP \(code)"))
                return
            }
            guard let data = data, let content = String(data: data, encoding: .utf8) else {
                result = .failure(I18nError.downloadFailed("Could not decode response as UTF-8"))
                return
            }
            result = .success(content)
        }.resume()

        semaphore.wait()
        return try result.get()
    }

    private static func md5(_ string: String) -> String {
        let data = Data(string.utf8)
        let digest = Insecure.MD5.hash(data: data)
        return digest.map { String(format: "%02x", $0) }.joined()
    }
}
