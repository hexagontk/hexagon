package com.hexagonkt.settings

import com.hexagonkt.helpers.Loggable
import com.hexagonkt.helpers.get
import com.hexagonkt.helpers.loggerOf
import org.slf4j.Logger

object SettingsManager : Loggable {
    override val log: Logger = loggerOf<SettingsManager>()

    private const val SETTINGS = "service"
    private const val ENVIRONMENT_PREFIX = "SERVICE_"

    val environment: String? get() = setting("${ENVIRONMENT_PREFIX}ENVIRONMENT")

    var settings: Map<String, *> = emptyMap<String, Any>()
        set(value) {
            val newEnvironment = value["${ENVIRONMENT_PREFIX}ENVIRONMENT"] as? String

            if (newEnvironment != null && environment != newEnvironment)
                field += value + loadResource("${newEnvironment.toLowerCase()}.yaml")
            else
                field = value
        }

    init {
        settings = loadDefaultSettings()
    }

    fun loadDefaultSettings(vararg args: String): Map<String, *> =
        loadResource("$SETTINGS.yaml") +
        loadEnvironmentVariables(ENVIRONMENT_PREFIX) +
        loadSystemProperties(SETTINGS) +
        loadFile("$SETTINGS.yaml") +
        loadCommandLineArguments(*args) +
        loadResource("${SETTINGS}_test.yaml")

    fun setDefaultSettings(vararg args: String) {
        settings = emptyMap<String, Any>()
        settings = loadDefaultSettings(*args)
    }

    @Suppress("UNCHECKED_CAST", "ReplaceGetOrSet")
    fun <T : Any> setting(vararg name: String): T? = settings.get(*name) as? T

    fun <T : Any> requireSetting(vararg name: String): T =
        setting(*name) ?: error("$name required setting not found")
}
