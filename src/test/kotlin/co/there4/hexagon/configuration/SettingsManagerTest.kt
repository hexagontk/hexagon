package co.there4.hexagon.configuration

import co.there4.hexagon.configuration.SettingsManager.setting
import org.testng.annotations.Test

/**
 * Must be the first to be run. If not, settings will be already loaded and will fail. Check
 * `gradle.build` to see the related files creation
 */
@Test class SettingsManagerTest {
    fun get_configuration_properties() {
        assert(SettingsManager["property"] == "changed")
        assert(setting<String>("property") == "changed")
        assert(setting<Int>("intProperty") == 42)
        assert(setting<String>("foo") == "bar")
    }
}
