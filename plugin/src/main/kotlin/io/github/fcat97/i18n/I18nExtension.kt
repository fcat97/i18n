package io.github.fcat97.i18n

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import java.io.File

abstract class I18nExtension {
    @get:Input
    @get:Optional
    abstract val sheetUrl: Property<String>

    @get:Input
    @get:Optional
    abstract val credentialsFile: RegularFileProperty

    @get:OutputDirectory
    @get:Optional
    abstract val outputDir: RegularFileProperty

    @get:OutputDirectory
    @get:Optional
    abstract val iosOutputDir: RegularFileProperty

    fun url(url: String) {
        sheetUrl.set(url)
    }

    fun credentialsFile(file: Any) {
        credentialsFile.set(file as File)
    }

    fun outputDir(dir: Any) {
        outputDir.set(dir as File)
    }

    fun iosOutputDir(dir: Any) {
        iosOutputDir.set(dir as File)
    }

    companion object {
        const val DEFAULT_OUTPUT_DIR = "build/generated/res/strings/main"
        const val DEFAULT_IOS_OUTPUT_DIR = "build/generated/ios/strings/main"
    }
}
