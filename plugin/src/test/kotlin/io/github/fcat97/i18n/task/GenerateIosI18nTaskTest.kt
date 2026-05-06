package io.github.fcat97.i18n.task

import kotlin.test.Test
import kotlin.test.assertNotNull
import org.gradle.testfixtures.ProjectBuilder

class GenerateIosI18nTaskTest {

    @Test
    fun taskIsRegisteredWithCorrectGroup() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("io.github.fcat97.i18n")

        val task = project.tasks.findByName("generateIosI18n")
        assertNotNull(task)
        assert(task!!.group == "i18n")
    }
}
