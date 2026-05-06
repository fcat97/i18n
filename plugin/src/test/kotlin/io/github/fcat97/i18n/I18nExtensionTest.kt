package io.github.fcat97.i18n

import kotlin.test.Test
import kotlin.test.assertTrue

class I18nExtensionTest {

    @Test
    fun defaultOutputDirConstantIsSet() {
        assertTrue(I18nExtension.DEFAULT_OUTPUT_DIR.isNotBlank())
    }

    @Test
    fun defaultIosOutputDirConstantIsSet() {
        assertTrue(I18nExtension.DEFAULT_IOS_OUTPUT_DIR.isNotBlank())
    }

    @Test
    fun defaultOutputDirIsAndroidResPath() {
        assertTrue(I18nExtension.DEFAULT_OUTPUT_DIR.contains("res"))
    }

    @Test
    fun defaultIosOutputDirIsIosPath() {
        assertTrue(I18nExtension.DEFAULT_IOS_OUTPUT_DIR.contains("ios"))
    }
}
