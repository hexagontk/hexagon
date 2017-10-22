package com.hexagonkt.settings

import com.hexagonkt.helpers.Loggable
import com.hexagonkt.helpers.get

object SettingsManager : Loggable {
//    val environment: String? get() = setting("ENVIRONMENT")

    var environment: String? = null
        private set

    var settings: Map<String, *> = defaultSettings()

//    init {
//        this.settings += "ENVIRONMENT" to "DEVELOPMENT"
//    }

    private fun defaultSettings(): Map<String, *> {
        val settings = loadResource("service.yaml") + loadResource("service_test.yaml")

//        this.settings += "ENVIRONMENT" to "DEVELOPMENT"

        environment = settings["ENVIRONMENT"] as? String
        environment = "DEVELOPMENT"

        return LinkedHashMap(
            if (environment != null)
                settings + loadResource("${environment?.toLowerCase()}.yaml")
            else
                settings
        )
    }

    @Suppress("UNCHECKED_CAST", "ReplaceGetOrSet")
    fun <T : Any> setting(vararg name: String): T? = settings.get(*name) as? T

    fun <T : Any> requireSetting(vararg name: String): T =
        setting(*name) ?: error("$name required setting not found")
}
