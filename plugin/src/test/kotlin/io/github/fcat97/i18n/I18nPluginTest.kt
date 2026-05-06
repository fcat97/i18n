package io.github.fcat97.i18n

import kotlin.test.Test
import kotlin.test.assertNotNull
import org.gradle.testfixtures.ProjectBuilder

class I18nPluginTest {

    @Test
    fun pluginRegistersGenerateI18nTask() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("io.github.fcat97.i18n")

        val task = project.tasks.findByName("generateI18n")
        assertNotNull(task)
    }

    @Test
    fun pluginRegistersGenerateIosI18nTask() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("io.github.fcat97.i18n")

        val task = project.tasks.findByName("generateIosI18n")
        assertNotNull(task)
    }
}
