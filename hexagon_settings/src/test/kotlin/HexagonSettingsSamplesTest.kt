package com.hexagonkt.settings

import org.junit.jupiter.api.Test

class HexagonSettingsSamplesTest {

    @Test fun settingsUsage() {
        // settingsUsage
        SettingsManager.settingsSources += ObjectSource(
            "stringProperty" to "str",
            "integerProperty" to 101,
            "booleanProperty" to true
        )

        assert(SettingsManager.settings["stringProperty"] == "str")
        assert(SettingsManager.settings["integerProperty"] == 101)
        assert(SettingsManager.settings["booleanProperty"] == true)
        // settingsUsage
    }
}
