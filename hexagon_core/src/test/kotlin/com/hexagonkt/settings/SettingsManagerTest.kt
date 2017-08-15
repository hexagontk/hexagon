package com.hexagonkt.settings

import com.hexagonkt.helpers.get
import com.hexagonkt.settings.SettingsManager.settings
import org.testng.annotations.Test

/**
 * Check `gradle.build` to see the related files creation.
 */
@Test class SettingsManagerTest {
    fun get_configuration_properties() {
        assert(settings["property"] as String == "changed")
        assert(settings["property"] as String == "changed")
        assert(settings["intProperty"] as Int == 42)
        assert(settings["foo"] as String == "bar")
        assert(settings["parent", "key"] as String == "val")
    }
}
