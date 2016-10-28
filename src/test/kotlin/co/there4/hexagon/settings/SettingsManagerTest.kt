package co.there4.hexagon.settings

import co.there4.hexagon.settings.SettingsManager.setting
import org.testng.annotations.ObjectFactory
import org.testng.annotations.Test

/**
 * Check `gradle.build` to see the related files creation.
 */
@Test class SettingsManagerTest {
    @ObjectFactory fun getObjectFactory() = org.powermock.modules.testng.PowerMockObjectFactory()

    fun power_mock() {
    }

    fun get_configuration_properties() {
        assert(SettingsManager["property"] == "changed")
        assert(setting<String>("property") == "changed")
        assert(setting<Int>("intProperty") == 42)
        assert(setting<String>("foo") == "bar")
        assert(setting<String>("parent", "key") == "val")
    }
}
