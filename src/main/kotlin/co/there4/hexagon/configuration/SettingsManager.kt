package co.there4.hexagon.configuration

import co.there4.hexagon.util.CompanionLogger
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
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
    private val mapper = ObjectMapper(YAMLFactory())

    val environment: Environment? = if (environmentFile.exists() && environmentFile.isFile) {
        val environmentContent = environmentFile.readText()
        info("Loading '$environmentContent' environment from '${environmentFile.absolutePath}'")
        Environment.valueOf(environmentContent.toUpperCase())
    }
    else {
        warn ("Environment not set")
        null
    }

    val parameters: Map<String, *> = loadParameters()

    /*
     * TODO Handle nested keys!
     */
    operator fun get (vararg key: String): Any? = parameters[key.first()]

    @Suppress("UNCHECKED_CAST")
    fun <T> setting(vararg key: String): T? = get(*key) as T?

    private fun loadParameters (): Map<String, *> {
        var params = loadProps("service.yaml")

        if (environment != null)
            params += loadProps("${environment.toString().toLowerCase()}.yaml")

        return params
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadProps (resName: String): Map<String, *> =
        systemClassLoader.getResourceAsStream(resName).let {
            if (it == null) mapOf<String, Any>()
            else mapper.readValue(it, Map::class.java) as Map<String, *>
        }
}
