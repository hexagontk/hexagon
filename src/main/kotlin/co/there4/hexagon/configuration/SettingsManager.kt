package co.there4.hexagon.configuration

import co.there4.hexagon.serialization.parse
import co.there4.hexagon.util.CompanionLogger
import java.io.File
import java.lang.ClassLoader.getSystemClassLoader
import java.lang.System.getProperty

enum class Environment { PRODUCTION, INTEGRATION, DEVELOPMENT }

/**
 * Reads:
 * - service.yaml (resource)
 * - <environment>.yaml (resource)
 * - service.yaml (file)
 * - <environment>.yaml (file)
 */
object SettingsManager : CompanionLogger(SettingsManager::class) {
    private val environmentFile = File("${getProperty("user.home")}/.environment")
    private val systemClassLoader = getSystemClassLoader()

    val environment: Environment? = if (environmentFile.exists() && environmentFile.isFile) {
        val environmentContent = environmentFile.readText().trim()
        info("Loading '$environmentContent' environment from '${environmentFile.absolutePath}'")
        Environment.valueOf(environmentContent.toUpperCase())
    }
    else {
        warn ("Environment not set")
        null
    }

    val parameters: Map<String, *> = loadProps("service.yaml") +
        if (environment != null) loadProps("${environment.toString().toLowerCase()}.yaml")
        else mapOf<String, Any>()

    operator fun get (vararg key: String): Any? = key
        .dropLast(1)
        .fold(parameters) { result, element ->
            @Suppress("UNCHECKED_CAST")
            (result[element] as Map<String, *>)
        }[key.last()]

    @Suppress("UNCHECKED_CAST")
    fun <T> setting(vararg key: String): T? = get(*key) as T?
    fun <T> requireSetting(vararg key: String): T =
        setting<T>(*key) ?: error ("Missing setting: $key")

    @Suppress("UNCHECKED_CAST")
    private fun loadProps (resName: String): Map<String, *> =
        systemClassLoader.getResourceAsStream(resName).let {
            if (it == null) {
                info("No environment settings found '$resName'")
                mapOf<String, Any>()
            }
            else {
                val props = it.parse(Map::class, "application/yaml") as Map<String, *>
                info("Settings loaded from '$resName'")
                props
            }
        }
}
