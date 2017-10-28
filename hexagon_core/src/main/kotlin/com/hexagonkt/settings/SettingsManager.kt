package com.hexagonkt.settings

import com.hexagonkt.helpers.Loggable
import com.hexagonkt.helpers.get

object SettingsManager : Loggable {
    val environment: String? get() = setting("SERVICE_ENVIRONMENT")

    var settings: Map<String, *> = emptyMap<String, Any>()
        set(value) {
            val newEnvironment = value["SERVICE_ENVIRONMENT"] as? String

            if (newEnvironment != null && environment != newEnvironment)
                field += value + loadResource("${newEnvironment.toLowerCase()}.yaml")
            else
                field = value
        }

    init {
        settings = loadDefaultSettings()
    }

    @Suppress("UNCHECKED_CAST")
    fun loadDefaultSettings(vararg args: String): Map<String, *> =
        loadResource("service.yaml") +
        loadEnvironmentVariables("SERVICE_") +
        loadSystemProperties("service") +
        loadFile("service.yaml") +
        loadCommandLineArguments(*args) +
        loadResource("service_test.yaml")

    @Suppress("UNCHECKED_CAST", "ReplaceGetOrSet")
    fun <T : Any> setting(vararg name: String): T? = settings.get(*name) as? T

    fun <T : Any> requireSetting(vararg name: String): T =
        setting(*name) ?: error("$name required setting not found")
}
