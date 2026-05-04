package com.portonics.i18n.task

import com.portonics.i18n.I18nExtension
import kotlin.reflect.full.memberProperties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerateI18nTaskTest {

    @Test
    fun taskHasSheetUrlProperty() {
        val taskClass = GenerateI18nTask::class
        val property = taskClass.memberProperties.find { it.name == "sheetUrl" }

        assertTrue(property != null, "sheetUrl property should exist")
    }

    @Test
    fun taskHasCredentialsFileProperty() {
        val taskClass = GenerateI18nTask::class
        val property = taskClass.memberProperties.find { it.name == "credentialsFile" }

        assertTrue(property != null, "credentialsFile property should exist")
    }

    @Test
    fun taskHasOutputDirProperty() {
        val taskClass = GenerateI18nTask::class
        val property = taskClass.memberProperties.find { it.name == "outputDir" }

        assertTrue(property != null, "outputDir property should exist")
    }

    @Test
    fun taskHasGenerateMethod() {
        val taskClass = GenerateI18nTask::class
        val method = taskClass.java.methods.any { it.name == "generate" }

        assertTrue(method, "generate method should exist")
    }

    @Test
    fun taskExtendsDefaultTask() {
        val taskClass = GenerateI18nTask::class
        val superclass = taskClass.java.superclass.name

        assertEquals("org.gradle.api.DefaultTask", superclass)
    }
}