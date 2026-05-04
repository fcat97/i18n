package com.portonics.i18n.task

import com.portonics.i18n.google.SheetsClient
import com.portonics.i18n.parser.ExcelParser
import com.portonics.i18n.generator.AndroidStringsGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateI18nTask : DefaultTask() {
    abstract val sheetUrl: Property<String>
    abstract val credentialsFile: RegularFileProperty
    abstract val outputDir: RegularFileProperty
    abstract val defaultLocale: Property<String>

    @TaskAction
    fun generate() {
        val url = sheetUrl.get()
        val output = outputDir.asFile.get()
        val credentials = credentialsFile.asFile.orNull
        val defaultLoc = if (defaultLocale.isPresent) defaultLocale.get() else "en"

        logger.quiet("Downloading sheet from: $url")

        val csvContent = if (credentials != null && credentials.exists()) {
            logger.quiet("Using service account credentials")
            SheetsClient.downloadWithCredentials(url, credentials)
        } else {
            logger.quiet("Downloading without credentials (public sheet)")
            SheetsClient.downloadPublicSheet(url)
        }

        val localizationData = ExcelParser.parse(csvContent)

        AndroidStringsGenerator.generate(localizationData, output, defaultLoc)

        logger.quiet("Generated localization files to: ${output.absolutePath}")
    }
}