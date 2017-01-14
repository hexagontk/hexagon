package co.there4.hexagon.settings

import co.there4.hexagon.settings.SettingsManager.requireSetting
import org.testng.annotations.Test
import kotlin.test.assertFailsWith

/**
 * Check `gradle.build` to see the related files creation.
 */
@Test class SettingsManagerTest {
    fun get_configuration_properties() {
        assert(SettingsManager["property"] == "changed")
        assert(requireSetting<String>("property") == "changed")
        assert(requireSetting<String>("property") == "changed")
        assert(requireSetting<Int>("intProperty") == 42)
        assert(requireSetting<String>("foo") == "bar")
        assert(requireSetting<String>("parent", "key") == "val")
        assertFailsWith<IllegalStateException>("Missing setting: absent") {
            requireSetting<String>("absent")
        }
    }
}
