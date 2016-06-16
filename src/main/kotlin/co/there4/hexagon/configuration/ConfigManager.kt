package co.there4.hexagon.configuration

import co.there4.hexagon.util.CompanionLogger
import co.there4.hexagon.util.hostname
import co.there4.hexagon.util.ip
import org.slf4j.MDC
import java.lang.ClassLoader.getSystemClassLoader
import java.lang.System.getenv
import java.lang.Thread.currentThread
import java.lang.management.ManagementFactory.getRuntimeMXBean
import java.util.*

enum class Environment { PRODUCTION, INTEGRATION, DEVELOPMENT }

/**
 * TODO Use JSON
 *
 * Reads:
 * - service.properties
 * - <project>.properties (resource)
 * - <project>_<environment>.properties (resource)
 * - <project>.properties (file)
 * - <project>_<environment>.properties (file)
 */
object ConfigManager : CompanionLogger(ConfigManager::class) {
    val jvmId = getRuntimeMXBean().name

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

    /** TODO This could be reset at the end of requests, maybe need to be moved to utils or log */
    init {
        MDC.put("jvmId", jvmId)
        MDC.put("hostname", hostname)
        MDC.put("ip", ip)
    }

    private fun loadParameters (): Map<String, *> {
        var params = loadProps("service.properties")

        if (environment != null)
            params += loadProps("${environment}.properties")

        return params
    }

    private fun getServiceClass (): Class<*> {
        val stack = currentThread().stackTrace
        val first = stack
            .filter { !it.className.startsWith("com.intellij") }
            .firstOrNull { it.methodName == "<clinit>" }

        return Class.forName (first?.className ?: error ("Main class not found"))
    }

    private fun loadProps (resName: String): Map<String, *> {
        val resource = getSystemClassLoader().getResourceAsStream(resName)
        return if (resource == null) {
            mapOf<String, Any>()
        }
        else {
            val v = Properties()
            v.load(resource)
            v
                .filter { it.key != null && it.key is String }
                .map { it.key as String to it.value }
                .toMap()
        }
    }

    operator fun get (key: String): Any? = parameters[key]

    fun stringParam(key: String): String? = get(key)?.let {
        when (it) {
            is String -> it
            else -> it.toString()
        }
    }

    fun intParam(key: String): Int? = get(key)?.let {
        when (it) {
            is String -> it.toInt()
            is Int -> it
            else -> error("Invalid type (${it.javaClass.name}) for ($key) parameter")
        }
    }
}
