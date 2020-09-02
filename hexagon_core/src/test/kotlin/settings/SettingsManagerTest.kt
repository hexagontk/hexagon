package com.hexagonkt.settings

import com.hexagonkt.helpers.get
import com.hexagonkt.serialization.Json
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.Yaml
import com.hexagonkt.settings.SettingsManager.ENVIRONMENT_PREFIX
import com.hexagonkt.settings.SettingsManager.SETTINGS
import com.hexagonkt.settings.SettingsManager.defaultSetting
import com.hexagonkt.settings.SettingsManager.settings
import com.hexagonkt.settings.SettingsManager.setting
import com.hexagonkt.settings.SettingsManager.requireSetting
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertFailsWith

/**
 * Check `gradle.build` to see the related files creation.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SettingsManagerTest {

    @BeforeAll fun setUpSerializationManager() {
        SerializationManager.formats = linkedSetOf(Json, Yaml)
    }

    @BeforeEach fun resetSettingSources() {
        SettingsManager.settingsSources = listOf(
            ResourceSource("classpath:$SETTINGS.yml"),
            ResourceSource("classpath:development.yml"),
            EnvironmentVariablesSource(ENVIRONMENT_PREFIX),
            SystemPropertiesSource(SETTINGS),
            FileSource("$SETTINGS.yml"),
            ResourceSource("classpath:${SETTINGS}_test.yml")
        )
    }

    @Test fun `Setting works as expected`() {
        assert(setting<String>("property") == "value")
        assert(setting<Int>("intProperty") == 42)
        assert(setting<String>("foo") == "bar")
    }

    @Test fun `Get configuration properties with defaults`() {
        assert(defaultSetting("fakeProperty", "changed") == "changed")
        assert(defaultSetting("fakeIntProperty", 42) == 42)
    }

    @Test fun `Get JSON properties`() {
        SettingsManager.settingsSources = listOf(
            ResourceSource("classpath:$SETTINGS.yml"),
            ResourceSource("classpath:development.yml"),
            EnvironmentVariablesSource(ENVIRONMENT_PREFIX),
            SystemPropertiesSource(SETTINGS),
            FileSource("$SETTINGS.yml"),
            ResourceSource("classpath:${SETTINGS}_test.yml"),
            ResourceSource("classpath:integration.json")
        )

        assert(settings["property"] as String == "final property")
        assert(settings["intProperty"] as Int == 42)
        assert(settings["foo"] as String == "final")
        assert(settings["parent", "key"] as String == "val")
        assert(settings["added"] as Boolean)
        assert(settings["integer"] as Int == 1)
    }

    @Test fun `Get configuration properties`() {
        SettingsManager.settingsSources = listOf(
            ResourceSource("classpath:$SETTINGS.yml"),
            ResourceSource("classpath:development.yml"),
            EnvironmentVariablesSource(ENVIRONMENT_PREFIX),
            SystemPropertiesSource(SETTINGS),
            FileSource("$SETTINGS.yml"),
            ResourceSource("classpath:${SETTINGS}_test.yml")
        )

        assert(settings["property"] as String == "value")
        assert(settings["intProperty"] as Int == 42)
        assert(settings["foo"] as String == "bar")
        assert(settings["parent", "key"] as String == "val")
    }

    @Test fun `Require configuration properties`() {
        assert(requireSetting<String>("property") == "value")
        assert(requireSetting<Int>("intProperty") == 42)
        assert(requireSetting<String>("foo") == "bar")
        assert(requireSetting<String>("parent", "key") == "val")
    }

    @Test fun `Require not found setting`() {
        assertFailsWith<IllegalStateException> {
            requireSetting<String>("not_found")
        }
    }

    @Test fun `Using the 'apply' shortcut works correctly`() {
        val localSettings = SettingsManager {
            settingsSources = settingsSources + ObjectSource(
                "stringProperty" to "str",
                "integerProperty" to 101,
                "booleanProperty" to true
            )
        }

        assert(localSettings.settings["stringProperty"] == "str")
        assert(localSettings.settings["integerProperty"] == 101)
        assert(localSettings.settings["booleanProperty"] == true)
    }

    @Test fun `Set default settings add command line arguments`() {
        SettingsManager.settingsSources = listOf(
            ResourceSource("classpath:$SETTINGS.yml"),
            EnvironmentVariablesSource(ENVIRONMENT_PREFIX),
            SystemPropertiesSource(SETTINGS),
            FileSource("$SETTINGS.yml"),
            ResourceSource("classpath:${SETTINGS}_test.yml"),
            CommandLineArgumentsSource(listOf("key=val", "param=data"))
        )

        assert(settings.size == 9)
        assert(requireSetting<String>("key") == "val")
        assert(requireSetting<String>("param") == "data")
        assert(requireSetting<String>("property") == "value")
    }
}
