package co.there4.hexagon.util

import org.slf4j.LoggerFactory.getLogger
import org.slf4j.MDC
import java.lang.System.nanoTime
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

    fun time (startNanos: Long, message: String?) = printNanos(message) { nanoTime() - startNanos }

    fun <T> time (message: String? = null, block: () -> T): T {
        val start = nanoTime()
        val result = block()
        time (start, message)
        return result
    }

    private fun printNanos(message: String? = null, lambda: () -> Long): Long = lambda().let {
        logger.trace((if (message != null) message + " : " else "TIME : ") + formatNanos(it))
        it
    }

    private fun wrapLog(log: () -> Unit) {
        MDC.put("jvmId", jvmId)
        MDC.put("hostname", hostname)
        MDC.put("ip", ip)
        log()
    }
}
