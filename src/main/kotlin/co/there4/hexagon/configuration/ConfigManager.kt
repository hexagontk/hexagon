package co.there4.hexagon.configuration

import co.there4.hexagon.util.camelToSnake
import java.io.File
import java.lang.ClassLoader.getSystemClassLoader
import java.lang.System.getenv
import java.lang.Thread.currentThread
import java.util.*

/**
 * Reads:
 * - application.properties (resource)
 * - <project>.properties (resource)
 * - <project>_<environment>.properties (resource)
 * - <project>.properties (file)
 * - <project>_<environment>.properties (file)
 * - System properties
 */
object ConfigManager {
    val mainClass = getServiceClass()
    val jarDir = File (mainClass.protectionDomain.codeSource.location.toURI())
    val serviceDir = jarDir.parent
    val servicePackage = mainClass.`package`.name
    val serviceName = mainClass.simpleName.camelToSnake().removeSuffix("_kt")

    private var parameters: Map<String, *> = loadProps ("application.properties")

    val environment: String? = getenv(parameters["environmentVariable"]?.toString() ?: "ENVIRONMENT")

    init {
        parameters = parameters + loadProps("${serviceName}.properties")
    }

    private fun getServiceClass (): Class<*> {
        val first = currentThread().stackTrace.firstOrNull { it.methodName == "main" }
        return if (first != null) Class.forName (first.className) else ConfigManager.javaClass
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

    operator fun get (key: String) = parameters[key]
}
