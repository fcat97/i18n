package com.portonics.i18n.task

import com.portonics.i18n.I18nExtension
import com.portonics.i18n.google.SheetsClient
import com.portonics.i18n.parser.ExcelParser
import com.portonics.i18n.generator.AndroidStringsGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateI18nTask : DefaultTask() {
    @get:Input
    abstract val sheetUrl: Property<String>

    @get:Internal
    abstract val credentialsFile: RegularFileProperty

    @get:Internal
    abstract val outputDir: RegularFileProperty

    @TaskAction
    fun generate() {
        val url = sheetUrl.get()
        val output = if (outputDir.isPresent) {
            outputDir.asFile.get()
        } else {
            project.file(I18nExtension.DEFAULT_OUTPUT_DIR)
        }
        val credentials = credentialsFile.asFile.orNull

        logger.quiet("[i18n] Starting localization generation")
        logger.quiet("[i18n] Sheet URL: $url")

        logger.quiet("[i18n] Downloading sheet data...")
        val csvContent = if (credentials != null && credentials.exists()) {
            logger.quiet("[i18n] Using service account credentials from: ${credentials.name}")
            SheetsClient.downloadWithCredentials(url, credentials)
        } else {
            logger.quiet("[i18n] No credentials provided, attempting to download public sheet")
            SheetsClient.downloadPublicSheet(url)
        }
        logger.quiet("[i18n] Download complete (${csvContent.lines().size} rows)")

        logger.quiet("[i18n] Parsing spreadsheet data...")
        val localizationData = ExcelParser.parse(csvContent)
        logger.quiet("[i18n] Default locale: ${localizationData.defaultLocale}")
        logger.quiet("[i18n] Additional locales: ${localizationData.additionalLocales.joinToString()}")
        logger.quiet("[i18n] Found ${localizationData.entries.size} string keys")

        logger.quiet("[i18n] Generating Android strings resources...")
        AndroidStringsGenerator.generate(localizationData, output)

        val generatedFiles = 1 + localizationData.additionalLocales.size
        logger.quiet("[i18n] Generated $generatedFiles localization files in: ${output.absolutePath}")
        logger.quiet("[i18n] Localization generation complete!")
    }
}