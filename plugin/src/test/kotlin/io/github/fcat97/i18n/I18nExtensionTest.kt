package io.github.fcat97.i18n

import kotlin.reflect.full.memberProperties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class I18nExtensionTest {

    @Test
    fun extensionIsAbstractClass() {
        val clazz = I18nExtension::class.java
        assertTrue(clazz.modifiers and java.lang.reflect.Modifier.ABSTRACT != 0)
    }

    @Test
    fun extensionHasSheetUrlProperty() {
        val properties = I18nExtension::class.memberProperties
        val hasSheetUrl = properties.any { it.name == "sheetUrl" }
        assertTrue(hasSheetUrl)
    }

    @Test
    fun extensionHasCredentialsFileProperty() {
        val properties = I18nExtension::class.memberProperties
        val hasCredentialsFile = properties.any { it.name == "credentialsFile" }
        assertTrue(hasCredentialsFile)
    }

    @Test
    fun extensionHasOutputDirProperty() {
        val properties = I18nExtension::class.memberProperties
        val hasOutputDir = properties.any { it.name == "outputDir" }
        assertTrue(hasOutputDir)
    }

    @Test
    fun defaultOutputDirConstantIsCorrect() {
        assertEquals("build/generated/res/strings/main", I18nExtension.DEFAULT_OUTPUT_DIR)
    }

    @Test
    fun urlMethodIsPresent() {
        val hasMethod = I18nExtension::class.java.methods.any { it.name == "url" }
        assertTrue(hasMethod)
    }

    @Test
    fun credentialsFileMethodIsPresent() {
        val hasMethod = I18nExtension::class.java.methods.any { it.name == "credentialsFile" }
        assertTrue(hasMethod)
    }

    @Test
    fun outputDirMethodIsPresent() {
        val hasMethod = I18nExtension::class.java.methods.any { it.name == "outputDir" }
        assertTrue(hasMethod)
    }
}