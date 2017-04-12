package co.there4.hexagon.util

import org.slf4j.LoggerFactory.getLogger
import org.slf4j.MDC
import java.lang.System.nanoTime
import kotlin.reflect.KClass

open class CompanionLogger(clazz: KClass<out Any>){
    private val logger = getLogger(clazz.java)

    fun trace (message: String, vararg arguments: Any) = wrap { logger.trace(message, arguments) }
    fun debug (message: String, vararg arguments: Any) = wrap { logger.debug(message, arguments) }
    fun info (message: String, vararg arguments: Any) = wrap { logger.info(message, arguments) }
    fun warn (message: String, vararg arguments: Any) = wrap { logger.warn(message, arguments) }
    fun err(message: String, vararg arguments: Any) = wrap { logger.error(message, arguments) }
    fun warn (message: String, exception: Throwable) = wrap { logger.warn(message, exception) }
    fun err(message: String, exception: Throwable) = wrap { logger.error(message, exception) }
    fun flare (message: String = "") = wrap { logger.trace("$flarePrefix $message") }
    fun time (startNanos: Long, message: String?) = printNanos(message) { nanoTime() - startNanos }

    fun <T> time (message: String? = null, block: () -> T): T {
        val start = nanoTime()
        val result = block()
        time (start, message)
        return result
    }

    private fun printNanos(message: String? = null, lambda: () -> Long): Long = lambda().also {
        logger.trace((if (message != null) "$message : " else "TIME : ") + formatNanos(it))
    }

    private fun wrap(log: () -> Unit) {
        MDC.put("jvmId", jvmId)
        MDC.put("hostname", hostname)
        MDC.put("ip", ip)
        log()
    }
}
