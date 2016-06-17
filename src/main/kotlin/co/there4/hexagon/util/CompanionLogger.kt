package co.there4.hexagon.util

import org.slf4j.LoggerFactory.getLogger
import org.slf4j.MDC
import kotlin.reflect.KClass

open class CompanionLogger(clazz: KClass<out Any>){
    private val logger = getLogger(clazz.java)

    fun trace (message: String, arguments: Map<String, *> = mapOf<String, Any>()) =
        wrapLog { logger.trace(message, arguments) }

    fun debug (message: String, arguments: Map<String, *> = mapOf<String, Any>()) =
        wrapLog { logger.debug(message, arguments) }

    fun info (message: String, arguments: Map<String, *> = mapOf<String, Any>()) =
        wrapLog { logger.info(message, arguments) }

    fun warn (message: String, arguments: Map<String, *> = mapOf<String, Any>()) =
        wrapLog { logger.warn(message, arguments) }

    fun err(message: String, arguments: Map<String, *> = mapOf<String, Any>()) =
        wrapLog { logger.error(message, arguments) }

    fun warn (message: String, exception: Throwable) = wrapLog { logger.warn(message, exception) }

    fun err(message: String, exception: Throwable) = wrapLog { logger.error(message, exception) }

    fun flare (message: String = "") = wrapLog { logger.trace("$flarePrefix $message") }

    private fun wrapLog(log: () -> Unit) {
        MDC.put("jvmId", jvmId)
        MDC.put("hostname", hostname)
        MDC.put("ip", ip)
        log()
    }
}
