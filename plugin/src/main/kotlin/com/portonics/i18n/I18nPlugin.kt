package com.portonics.i18n

import com.portonics.i18n.task.GenerateI18nTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class I18nPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("i18n", I18nExtension::class.java)

        target.tasks.register(
            "generateI18n",
            GenerateI18nTask::class.java
        ) { task ->
            task.group = "i18n"
            task.description = "Generates localization files from Google Sheets"

            task.sheetUrl.convention(extension.sheetUrl)
            task.credentialsFile.convention(extension.credentialsFile)
            task.outputDir.convention(extension.outputDir)
            task.defaultLocale.convention(extension.defaultLocale)
        }

        target.afterEvaluate {
            if (!extension.sheetUrl.isPresent) {
                throw IllegalStateException("i18n.url must be configured")
            }
        }
    }
}