package com.hexagonkt.settings

import com.hexagonkt.helpers.get
import com.hexagonkt.logging.Logger
import com.hexagonkt.serialization.Json
import com.hexagonkt.serialization.serialize

object SettingsManager {

    val log: Logger = Logger(this)

    internal const val SETTINGS = "application"
    internal const val ENVIRONMENT_PREFIX = "application_"

    private val defaultSettingsSources: List<SettingsSource> = listOf(
        UrlSource("classpath:$SETTINGS.json"),
        UrlSource("classpath:$SETTINGS.yml"),
        EnvironmentVariablesSource(ENVIRONMENT_PREFIX),
        SystemPropertiesSource(SETTINGS),
        UrlSource("file:$SETTINGS.yml"),
        UrlSource("classpath:${SETTINGS}_test.json"),
        UrlSource("classpath:${SETTINGS}_test.yml")
    )

    var settingsSources: List<SettingsSource> = defaultSettingsSources
        set(value) {
            field = value
            settings = loadDefaultSettings()
        }

    var settings: Map<String, *> = loadDefaultSettings()
        private set

    @Suppress("UNCHECKED_CAST", "ReplaceGetOrSet")
    fun <T : Any> setting(vararg name: String): T? = settings.get(*name) as? T

    fun <T : Any> defaultSetting(name: String, value: T): T =
        defaultSetting(listOf(name), value)

    fun <T : Any> defaultSetting(name: List<String>, value: T): T =
        setting(*name.toTypedArray()) ?: value

    fun <T : Any> requireSetting(vararg name: String): T =
        setting(*name) ?: error("$name required setting not found")

    operator fun invoke(block: SettingsManager.() -> Unit): SettingsManager {
        this.apply(block)
        return this
    }

    private fun loadDefaultSettings(): Map<String, *> =
        settingsSources
            .map {
                it.load().also { s ->
                    if (s.isEmpty()) {
                        log.info { "No settings found for $it" }
                    }
                    else {
                        val serialize = s.serialize(Json).prependIndent(" ".repeat(4))
                        log.info { "Settings loaded from $it:\n\n$serialize" }
                    }
                }
            }
            .reduce { a, b -> a + b }
}
