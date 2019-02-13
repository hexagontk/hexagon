package com.hexagonkt.settings

import com.hexagonkt.helpers.get
import com.hexagonkt.settings.SettingsManager.ENVIRONMENT_PREFIX
import com.hexagonkt.settings.SettingsManager.SETTINGS
import com.hexagonkt.settings.SettingsManager.defaultSetting
import com.hexagonkt.settings.SettingsManager.settings
import com.hexagonkt.settings.SettingsManager.setting
import com.hexagonkt.settings.SettingsManager.requireSetting
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

/**
 * Check `gradle.build` to see the related files creation.
 */
@Test class SettingsManagerTest {

    @BeforeMethod fun resetSettingSources() {
        SettingsManager.settingsSources = listOf(
            ResourceSource("$SETTINGS.yaml"),
            ResourceSource("development.yaml"),
            EnvironmentVariablesSource(ENVIRONMENT_PREFIX),
            SystemPropertiesSource(SETTINGS),
            FileSource("$SETTINGS.yaml"),
            ResourceSource("${SETTINGS}_test.yaml")
        )
    }

    fun `Setting works as expected`() {
        assert(setting<String>("property") == "changed")
        assert(setting<Int>("intProperty") == 42)
        assert(setting<String>("foo") == "bar")
    }

    fun `Get configuration properties with defaults`() {
        assert(defaultSetting("fakeProperty", "changed") == "changed")
        assert(defaultSetting("fakeIntProperty", 42) == 42)
    }

    fun `Get configuration properties`() {
        SettingsManager.settingsSources = listOf(
            ResourceSource("$SETTINGS.yaml"),
            ResourceSource("development.yaml"),
            EnvironmentVariablesSource(ENVIRONMENT_PREFIX),
            SystemPropertiesSource(SETTINGS),
            FileSource("$SETTINGS.yaml"),
            ResourceSource("${SETTINGS}_test.yaml")
        )

        assert(settings["property"] as String == "changed")
        assert(settings["intProperty"] as Int == 42)
        assert(settings["foo"] as String == "bar")
        assert(settings["parent", "key"] as String == "val")
    }

    fun `Require configuration properties`() {
        assert(requireSetting<String>("property") == "changed")
        assert(requireSetting<Int>("intProperty") == 42)
        assert(requireSetting<String>("foo") == "bar")
        assert(requireSetting<String>("parent", "key") == "val")
    }

    @Test(expectedExceptions = [ IllegalStateException::class ])
    fun `Require not found setting`() {
        requireSetting<String>("not_found")
    }

    fun `Set default settings add command line arguments`() {
        SettingsManager.settingsSources = listOf(
            ResourceSource("$SETTINGS.yaml"),
            EnvironmentVariablesSource(ENVIRONMENT_PREFIX),
            SystemPropertiesSource(SETTINGS),
            FileSource("$SETTINGS.yaml"),
            ResourceSource("${SETTINGS}_test.yaml"),
            CommandLineArgumentsSource(listOf("key=val", "param=data"))
        )

        assert(settings.size == 12)
        assert(requireSetting<String>("key") == "val")
        assert(requireSetting<String>("param") == "data")
        assert(requireSetting<String>("property") == "value")
    }
}
