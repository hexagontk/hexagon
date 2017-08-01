package co.there4.hexagon.settings

import co.there4.hexagon.serialization.parse
import co.there4.hexagon.helpers.CachedLogger
import co.there4.hexagon.helpers.EOL
import co.there4.hexagon.helpers.resourceAsStream
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

    private val prefix = getProperty("settings.prefix") ?: ""
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

    private fun loadSettings() =
        loadProps("${prefix}service.yaml") +
        loadProps("${prefix}service_test.yaml") +
        if (environment != null) loadProps("$prefix${environment.toString().toLowerCase()}.yaml")
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
