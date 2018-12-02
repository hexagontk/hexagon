package com.hexagonkt.settings

import com.hexagonkt.helpers.get
import com.hexagonkt.helpers.Logger
import com.hexagonkt.serialization.YamlFormat
import com.hexagonkt.serialization.serialize

object SettingsManager {

    val log: Logger = Logger(this)

    internal const val SETTINGS = "service"
    internal const val ENVIRONMENT_PREFIX = "SERVICE_"

    val defaultSettingsSources: List<SettingsSource> = listOf(
        ResourceSource("$SETTINGS.yaml"),
        EnvironmentVariablesSource(ENVIRONMENT_PREFIX),
        SystemPropertiesSource(SETTINGS),
        FileSource("$SETTINGS.yaml"),
//        CommandLineArgumentsSource(*args),
        ResourceSource("${SETTINGS}_test.yaml")
    )

    var settingsSources: List<SettingsSource> = defaultSettingsSources
        set(value) {
            field = value
            settings = loadDefaultSettings()
        }

    var settings: Map<String, *> = loadDefaultSettings()
        private set(value) {
            field = value
        }

    @Suppress("UNCHECKED_CAST", "ReplaceGetOrSet")
    fun <T : Any> setting(vararg name: String): T? = settings.get(*name) as? T

    fun <T : Any> defaultSetting(name: String, value: T): T =
        defaultSetting(listOf(name), value)

    fun <T : Any> defaultSetting(name: List<String>, value: T): T =
        setting(*name.toTypedArray()) ?: value

    fun <T : Any> requireSetting(vararg name: String): T =
        setting(*name) ?: error("$name required setting not found")

    private fun loadDefaultSettings(): Map<String, *> =
        settingsSources
            .map {
                it.load().also { s ->
                    if (s.isEmpty()) {
                        log.info { "No settings found for $it" }
                    }
                    else {
                        val serialize = s.serialize(YamlFormat).prependIndent(" ".repeat(4))
                        log.info { "Settings loaded from $it:\n\n$serialize" }
                    }
                }
            }
            .reduce { a, b -> b + a }
}
