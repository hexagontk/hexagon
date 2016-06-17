package co.there4.hexagon.configuration

import co.there4.hexagon.util.CompanionLogger
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.lang.ClassLoader.getSystemClassLoader
import java.lang.System.getenv

enum class Environment { PRODUCTION, INTEGRATION, DEVELOPMENT }

/**
 * TODO Change environment config for a file (~/.environment) instead an environment variable
 *
 * Reads:
 * - service.yaml (resource)
 * - <environment>.yaml (resource)
 * - service.yaml (file)
 * - <environment>.yaml (file)
 */
object SettingsManager : CompanionLogger(SettingsManager::class) {
    private val systemClassLoader = getSystemClassLoader()
    private val mapper = ObjectMapper(YAMLFactory())
    private val parameters: Map<String, *> = loadParameters()

    val environment: Environment? = getenv("ENVIRONMENT").let {
        if (it == null) {
            warn ("Environment not set")
            null
        }
        else {
            Environment.valueOf(it)
        }
    }

    private fun loadParameters (): Map<String, *> {
        var params = loadProps("service.yaml")

        if (environment != null)
            params += loadProps("${environment}.properties")

        return params
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadProps (resName: String): Map<String, *> =
        systemClassLoader.getResourceAsStream(resName).let {
            if (it == null) mapOf<String, Any>()
            else mapper.readValue(it, Map::class.java) as Map<String, *>
        }

    /*
     * TODO Handle nested keys!
     */
    operator fun get (vararg key: String): Any? = parameters[key.first()]

    @Suppress("UNCHECKED_CAST")
    fun <T> setting(vararg key: String): T? = get(*key) as T?
}
