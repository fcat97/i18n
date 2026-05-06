package io.github.fcat97.i18n

import io.github.fcat97.i18n.task.GenerateI18nTask
import io.github.fcat97.i18n.task.GenerateIosI18nTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class I18nPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("i18n", I18nExtension::class.java)

        target.tasks.register("generateI18n", GenerateI18nTask::class.java) { task ->
            task.group = "i18n"
            task.description = "Generates Android localization files from Google Sheets"
            task.sheetUrl.convention(extension.sheetUrl)
            task.credentialsFile.convention(extension.credentialsFile)
            task.outputDir.convention(extension.outputDir)
        }

        target.tasks.register("generateIosI18n", GenerateIosI18nTask::class.java) { task ->
            task.group = "i18n"
            task.description = "Generates iOS localization files from Google Sheets"
            task.sheetUrl.convention(extension.sheetUrl)
            task.credentialsFile.convention(extension.credentialsFile)
            task.outputDir.convention(extension.iosOutputDir)
        }

        target.afterEvaluate {
            if (!extension.sheetUrl.isPresent) {
                throw IllegalStateException("i18n.url must be configured")
            }

            val generateAndroidTask = target.tasks.findByName("generateI18n") as? GenerateI18nTask

            val buildVariants = listOf("debug", "release")
            buildVariants.forEach { variant ->
                val assembleTask = target.tasks.findByName("assemble${variant.replaceFirstChar { it.uppercase() }}")
                if (assembleTask != null && generateAndroidTask != null) {
                    assembleTask.dependsOn(generateAndroidTask)
                }
            }

            val preBuildTask = target.tasks.findByName("preBuild")
            if (preBuildTask != null && generateAndroidTask != null) {
                preBuildTask.dependsOn(generateAndroidTask)
            }
        }
    }
}
