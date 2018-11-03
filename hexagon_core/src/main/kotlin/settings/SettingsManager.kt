package com.hexagonkt.settings

import com.hexagonkt.helpers.get
import com.hexagonkt.helpers.Logger
import com.hexagonkt.helpers.Resource
import com.hexagonkt.helpers.logger
import com.hexagonkt.serialization.YamlFormat
import com.hexagonkt.serialization.serialize
import java.io.File

object SettingsManager {

    val log: Logger = logger()

    private const val SETTINGS = "service"
    private const val ENVIRONMENT_PREFIX = "SERVICE_"

    var settingsSources: List<SettingsSource> = listOf(
        ResourceSource(Resource("$SETTINGS.yaml")),
        EnvironmentVariablesSource(listOf(ENVIRONMENT_PREFIX)),
        SystemPropertiesSource(listOf(SETTINGS)),
        FileSource(File("$SETTINGS.yaml")),
//        CommandLineArgumentsSource(*args),
        ResourceSource(Resource("${SETTINGS}_test.yaml"))
    )
    set(value) {
        settings = loadDefaultSettings()
    }

    var settings: Map<String, *> = loadDefaultSettings()
        private set(value) {
            field = value
        }

    @Suppress("UNCHECKED_CAST", "ReplaceGetOrSet")
    fun <T : Any> setting(vararg name: String): T? = settings.get(*name) as? T

    fun <T : Any> requireSetting(vararg name: String): T =
        setting(*name) ?: error("$name required setting not found")

    private fun loadDefaultSettings(): Map<String, *> =
        settingsSources
            .map {
                it.load().also { s ->
                    if (s.isEmpty()) {
                        SettingsManager.log.info { "No settings found for $it" }
                    }
                    else {
                        val serialize = s.serialize(YamlFormat).prependIndent(" ".repeat(4))
                        SettingsManager.log.info { "Settings loaded from $it:\n\n$serialize" }
                    }
                }
            }
            .reduceRight { a, b -> a + b }
}
