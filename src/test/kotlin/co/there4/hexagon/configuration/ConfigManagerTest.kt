package co.there4.hexagon.configuration

import co.there4.hexagon.configuration.ConfigManager.stringParam
import co.there4.hexagon.configuration.ConfigManager.intParam
import org.testng.annotations.Test

@Test class ConfigManagerTest {
    fun get_configuration_properties() {
        assert(ConfigManager["property"] == "value")
        assert(stringParam("property") == "value")
        assert(intParam("intProperty") == 42)
    }
}
