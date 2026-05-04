package com.portonics.i18n

import org.gradle.api.Plugin
import kotlin.test.Test
import kotlin.test.assertTrue

class I18nPluginTest {

    @Test
    fun pluginImplementsPluginInterface() {
        val pluginClass = I18nPlugin::class.java
        val interfaces = pluginClass.interfaces

        assertTrue(Plugin::class.java.isAssignableFrom(pluginClass))
    }

    @Test
    fun pluginHasApplyMethod() {
        val pluginClass = I18nPlugin::class.java
        val hasApply = pluginClass.methods.any {
            it.name == "apply" && it.parameterTypes.size == 1
        }

        assertTrue(hasApply, "apply method should exist")
    }

    @Test
    fun i18nExtensionClassExists() {
        assertTrue(I18nExtension::class.java != null)
    }

    @Test
    fun extensionIsAbstractClass() {
        assertTrue(I18nExtension::class.java.modifiers and java.lang.reflect.Modifier.ABSTRACT != 0)
    }
}