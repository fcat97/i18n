# i18n-gradle-plugin

<a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-1.9.22-blue.svg" alt="Kotlin"></a>
<a href="https://gradle.org/"><img src="https://img.shields.io/badge/Gradle-8.5-blue.svg" alt="Gradle"></a>
<a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg" alt="License"></a>
<a href="https://github.com/fcat97/i18n/actions"><img src="https://img.shields.io/badge/Tests-39%20passing-brightgreen" alt="Tests"></a>

A Gradle plugin that automatically generates Android localization strings from Google Sheets. Manage your app translations in a spreadsheet, and let the plugin do the rest!

## Why Use This Plugin?

- **Single Source of Truth** - Edit translations in Google Sheets, not code
- **Automatic Sync** - Run one command to fetch and generate all strings
- **Multi-language Support** - Add as many languages as you need
- **Developer Friendly** - No manual copy-pasting of string files

## Quick Start

### 1. Add the Plugin

In your root `build.gradle` (or `build.gradle.kts`):

```groovy
plugins {
    id 'io.github.fcat97.i18n' version '1.0.0' apply false
}
```

In your app module's `build.gradle`:

```groovy
plugins {
    id 'io.github.fcat97.i18n'
}
```

### 2. Configure Your Google Sheet

Set up your Google Sheet with this format:

| key | platform | default | en | my | zh |
|-----|----------|---------|----|----|----|
| app_name | android | My App | My App | မြန်မာပါတ် | 我的应用 |
| welcome | android | Welcome | Welcome | မင်္ဂလာပါ | 欢迎 |

> **Tip:** Make your sheet public (File → Share → Publish to web → CSV) OR use service account credentials.

### 3. Configure the Plugin

```groovy
i18n {
    url = "https://docs.google.com/spreadsheets/d/YOUR_SHEET_ID/edit"
    outputDir = file("src/main/res")
}
```

### 4. Run the Task

```bash
./gradlew generateI18n
```

That's it! Your Android `strings.xml` files are generated automatically.

## Configuration Options

```groovy
i18n {
    // Required: Your Google Sheet URL (must be publicly accessible or use credentials)
    url = "https://docs.google.com/spreadsheets/d/abc123..."

    // Optional: Output directory (default: build/generated/res/strings/main)
    outputDir = file("src/main/res")

    // Optional: Service account credentials file for private sheets
    credentialsFile = file("path/to/credentials.json")
}
```

## How It Works

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Google Sheet   │────▶│  generateI18n    │────▶│   strings.xml   │
│  (Your Translations)   │  Gradle Task     │     │  (Android Files)│
└─────────────────┘     └──────────────────┘     └─────────────────┘
        1. Download              2. Parse              3. Generate
```

**Step-by-step flow:**

1. **Download** - Fetches CSV data from your Google Sheet
2. **Parse** - Reads translations and extracts keys, values, and languages
3. **Generate** - Creates Android `values/`, `values-my/`, `values-zh/` folders with `strings.xml`

## Spreadsheet Format

Your Google Sheet should have:

| Column | Required | Description |
|--------|----------|-------------|
| `key` | ✅ Yes | Unique identifier (e.g., `app_name`, `welcome_message`) |
| `platform` | ✅ Yes | Target platform (`android`, `ios`, or `android,ios`) |
| `default` | ✅ Yes | Default/fallback value |
| `en`, `my`, `zh`, etc. | Optional | Translations for each language |

### Example Spreadsheet

| key | platform | default | en | my | zh |
|-----|----------|---------|----|----|----|
| app_name | android | My App | My App | မြန်မာပါတ် | 我的应用 |
| btn_submit | android | Submit | Submit | ပါးမ်း | 提交 |
| error_network | android | Network Error | Network Error | ပါလီမနားပါး | 网络错误 |

## Supported Languages

The plugin supports any language code. Common examples:

| Code | Language |
|------|----------|
| `en` | English |
| `my` | Burmese |
| `zh` | Chinese |
| `es` | Spanish |
| `fr` | French |
| `de` | German |
| `ja` | Japanese |
| `ko` | Korean |

Just add a column with the language code in your spreadsheet!

## Running Tests

```bash
# Run all tests
./gradlew test

# Run tests with coverage report
./gradlew test koverHtmlReport

# View coverage report
# Open: plugin/build/reports/kover/html/index.html
```

## Project Structure

```
i18n-gradle/
├── plugin/                    # The Gradle plugin
│   └── src/
│       ├── main/kotlin/       # Plugin source code
│       └── test/kotlin/       # Unit tests (39 tests)
├── demo/                      # Demo Android app
├── LICENSE                    # Apache 2.0 License
└── README.md                  # This file
```

## Built With

- [Kotlin](https://kotlinlang.org/) - Programming language
- [Gradle](https://gradle.org/) - Build automation
- [Kover](https://github.com/Kotlin/kotlinx-kover) - Test coverage

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

Made with ❤️ for the Android developer community