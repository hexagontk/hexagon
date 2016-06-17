package co.there4.hexagon.configuration

import co.there4.hexagon.configuration.SettingsManager.setting
import org.testng.annotations.Test

@Test class ConfigManagerTest {
    fun get_configuration_properties() {
        assert(SettingsManager["property"] == "value")
        assert(setting<String>("property") == "value")
        assert(setting<Int>("intProperty") == 42)
    }
}
