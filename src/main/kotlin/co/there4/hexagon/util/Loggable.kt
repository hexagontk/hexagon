package co.there4.hexagon.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.slf4j.MDC
import java.lang.System.nanoTime

/**
 * Ease the logger definition and usage. Note the logger is fetched in each call.
 *
 * TODO Check performance penalty of fetching the logger instead storing it.
 */
interface Loggable {
    fun logger(): Logger = getLogger(this::class.java)

    fun trace (message: String, vararg arguments: Any) =
        logger().apply { if (isTraceEnabled) wrap { trace(message, arguments) } }

    fun debug (message: String, vararg arguments: Any) =
        logger().apply { if (isDebugEnabled) wrap { debug(message, arguments) } }

    fun info (message: String, vararg arguments: Any) =
        logger().apply { if (isInfoEnabled) wrap { info(message, arguments) } }

    fun warn (message: String, vararg arguments: Any) =
        logger().apply { if (isWarnEnabled) wrap { warn(message, arguments) } }

    fun err(message: String, vararg arguments: Any) =
        logger().apply { if (isErrorEnabled) wrap { error(message, arguments) } }

    fun warn (message: String, exception: Throwable) =
        logger().apply { if (isWarnEnabled) wrap { warn(message, exception) } }

    fun error(message: String, exception: Throwable) =
        logger().apply { if (isErrorEnabled) wrap { error(message, exception) } }

    fun flare (message: String = "") =
        logger().apply { if (isTraceEnabled) wrap { trace("$flarePrefix $message") } }

    fun time (startNanos: Long, message: String?) =
        logger().apply { if (isTraceEnabled) printNanos(message) { nanoTime() - startNanos } }

    fun <T> time (message: String? = null, block: () -> T): T {
        val start = nanoTime()
        val result = block()
        time (start, message)
        return result
    }

    private fun printNanos(message: String? = null, lambda: () -> Long): Long = lambda().also {
        logger().trace((if (message != null) "$message : " else "TIME : ") + formatNanos(it))
    }

    private fun wrap(log: () -> Unit) {
        MDC.put("jvmId", jvmId)
        MDC.put("hostname", hostname)
        MDC.put("ip", ip)
        log()
    }
}
