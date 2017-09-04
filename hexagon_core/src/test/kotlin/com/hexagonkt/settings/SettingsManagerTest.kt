package com.hexagonkt.settings

import com.hexagonkt.helpers.get
import com.hexagonkt.settings.SettingsManager.settings
import com.hexagonkt.settings.SettingsManager.setting
import com.hexagonkt.settings.SettingsManager.requireSetting
import org.testng.annotations.Test

/**
 * Check `gradle.build` to see the related files creation.
 */
@Test class SettingsManagerTest {
    fun setting_works_as_expected() {
        assert(setting<String>("property") == "changed")
        assert(setting<Int>("intProperty") == 42)
        assert(setting<String>("foo") == "bar")
    }

    fun get_configuration_properties() {
        assert(settings["property"] as String == "changed")
        assert(settings["intProperty"] as Int == 42)
        assert(settings["foo"] as String == "bar")
        assert(settings["parent", "key"] as String == "val")
    }

    fun require_configuration_properties() {
        assert(requireSetting<String>("property") == "changed")
        assert(requireSetting<Int>("intProperty") == 42)
        assert(requireSetting<String>("foo") == "bar")
        assert(requireSetting<String>("parent", "key") == "val")
    }

    @Test(expectedExceptions = arrayOf(IllegalStateException::class))
    fun require_not_found_setting() {
        requireSetting<String>("not_found")
    }
}
