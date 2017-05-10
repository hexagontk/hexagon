package co.there4.hexagon.settings

import co.there4.hexagon.helpers.get
import co.there4.hexagon.settings.SettingsManager.settings
import org.testng.annotations.Test

/**
 * Check `gradle.build` to see the related files creation.
 */
@Test class SettingsManagerTest {
    fun get_configuration_properties() {
        assert(SettingsManager["property"] == "changed")
        assert(settings["property"] as String == "changed")
        assert(settings["property"] as String == "changed")
        assert(settings["intProperty"] as Int == 42)
        assert(settings["foo"] as String == "bar")
        assert(settings["parent", "key"] as String == "val")
    }
}
