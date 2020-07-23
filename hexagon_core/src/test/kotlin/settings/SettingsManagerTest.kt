package com.hexagonkt.settings

import com.hexagonkt.helpers.get
import com.hexagonkt.settings.SettingsManager.ENVIRONMENT_PREFIX
import com.hexagonkt.settings.SettingsManager.SETTINGS
import com.hexagonkt.settings.SettingsManager.defaultSetting
import com.hexagonkt.settings.SettingsManager.settings
import com.hexagonkt.settings.SettingsManager.setting
import com.hexagonkt.settings.SettingsManager.requireSetting
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Check `gradle.build` to see the related files creation.
 */
class SettingsManagerTest {

    @BeforeEach fun resetSettingSources() {
        SettingsManager.settingsSources = listOf(
            ResourceSource("$SETTINGS.yml"),
            ResourceSource("development.yml"),
            EnvironmentVariablesSource(ENVIRONMENT_PREFIX),
            SystemPropertiesSource(SETTINGS),
            FileSource("$SETTINGS.yml"),
            ResourceSource("${SETTINGS}_test.yml")
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

    @Test fun `Get configuration properties`() {
        SettingsManager.settingsSources = listOf(
            ResourceSource("$SETTINGS.yml"),
            ResourceSource("development.yml"),
            EnvironmentVariablesSource(ENVIRONMENT_PREFIX),
            SystemPropertiesSource(SETTINGS),
            FileSource("$SETTINGS.yml"),
            ResourceSource("${SETTINGS}_test.yml")
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
        shouldThrow<IllegalStateException> {
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
            ResourceSource("$SETTINGS.yml"),
            EnvironmentVariablesSource(ENVIRONMENT_PREFIX),
            SystemPropertiesSource(SETTINGS),
            FileSource("$SETTINGS.yml"),
            ResourceSource("${SETTINGS}_test.yml"),
            CommandLineArgumentsSource(listOf("key=val", "param=data"))
        )

        assert(settings.size == 9)
        assert(requireSetting<String>("key") == "val")
        assert(requireSetting<String>("param") == "data")
        assert(requireSetting<String>("property") == "value")
    }
}
