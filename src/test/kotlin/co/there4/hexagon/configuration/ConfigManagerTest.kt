package co.there4.hexagon.configuration

import co.there4.hexagon.configuration.ConfigManager.stringParam
import co.there4.hexagon.configuration.ConfigManager.intParam
import co.there4.hexagon.util.CompanionLogger
import org.testng.annotations.Test

@Test class ConfigManagerTest {
    companion object : CompanionLogger (ConfigManagerTest::class)

    fun get_configuration_properties() {
        info(">>>>>>>>>>>>>>>> ${ConfigManager.serviceName}")
        assert(ConfigManager["property"] == "value")
        assert(stringParam("property") == "value")
        assert(intParam("intProperty") == 42)
    }
}
