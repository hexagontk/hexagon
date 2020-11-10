package com.hexagonkt.settings

import org.junit.jupiter.api.Test

class HexagonSettingsSamplesTest {

    @Test fun settingsUsage() {
        // settingsUsage
        data class Configuration(
            val stringProperty: String,
            val integerProperty: Int,
            val booleanProperty: Boolean,
        )

        SettingsManager.settings = Settings(
            Configuration::class,
            ObjectSource(
                "stringProperty" to "str",
                "integerProperty" to 101,
                "booleanProperty" to true
            )
        )

        val configuration = SettingsManager.instance<Configuration>()
        assert(configuration.stringProperty == "str")
        assert(configuration.integerProperty == 101)
        assert(configuration.booleanProperty)
        // settingsUsage
    }
}
