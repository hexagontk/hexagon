package com.hexagonkt.settings

import com.hexagonkt.serialization.parse
import com.hexagonkt.helpers.get
import com.hexagonkt.helpers.CachedLogger
import com.hexagonkt.helpers.EOL
import com.hexagonkt.helpers.resourceAsStream
import java.io.File
import java.lang.System.getProperty

/**
 * 1. Takes project and module name from META-INF/manifest.mf
 * 2. Load environment
 *
 * Reads:
 * - service.yaml (resource)
 * - <environment>.yaml (resource)
 * - service.yaml (file)
 * - <environment>.yaml (file)
 */
open class Settings {
    private companion object : CachedLogger(Settings::class)

    private val environmentFile = File("${getProperty("user.home")}/.environment")

    val environment: Environment? = if (environmentFile.exists() && environmentFile.isFile) {
        val environmentContent = environmentFile.readText().trim()
        info("Loading '$environmentContent' environment from '${environmentFile.absolutePath}'")
        Environment.valueOf(environmentContent.toUpperCase())
    }
    else {
        warn ("Environment not set")
        null
    }

    val settings: Map<String, *> = loadSettings()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> setting(vararg name: String): T? = settings[name] as? T

    fun <T : Any> requireSetting(vararg name: String): T? =
        setting(*name) ?: error("$name required setting not found")

    private fun loadSettings() =
        loadProps("service.yaml") +
        loadProps("service_test.yaml") +
        if (environment != null) loadProps("${environment.toString().toLowerCase()}.yaml")
        else mapOf<String, Any>()

    @Suppress("UNCHECKED_CAST")
    private fun loadProps (resName: String): Map<String, *> =
        resourceAsStream(resName).let {
            if (it == null) {
                info("No environment settings found '$resName'")
                mapOf<String, Any>()
            }
            else {
                val props = it.parse(Map::class, "application/yaml") as Map<String, *>
                val separator = EOL + " ".repeat(4)
                info("Settings loaded from '$resName':" +
                    props.map { it.key + " : " + it.value }.joinToString(separator, separator, EOL)
                )
                props
            }
        }
}
