package com.hexagonkt.settings

import com.hexagonkt.helpers.Loggable
import com.hexagonkt.helpers.get

object SettingsManager : Loggable {
    val environment: String? get() = setting("ENVIRONMENT")

    var settings: Map<String, *> = emptyMap<String, Any>()
        set(value) {
            val newEnvironment = value["ENVIRONMENT"] as? String

            if (newEnvironment != null && environment != newEnvironment)
                field += value + loadResource("${newEnvironment.toLowerCase()}.yaml")
            else
                field = value
        }

    init {
        settings = loadResource("service.yaml") + loadResource("service_test.yaml")
        settings += "ENVIRONMENT" to "DEVELOPMENT" // TODO Move from here
    }

    @Suppress("UNCHECKED_CAST", "ReplaceGetOrSet")
    fun <T : Any> setting(vararg name: String): T? = settings.get(*name) as? T

    fun <T : Any> requireSetting(vararg name: String): T =
        setting(*name) ?: error("$name required setting not found")
}
