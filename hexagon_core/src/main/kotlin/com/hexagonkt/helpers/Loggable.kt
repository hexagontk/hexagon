package com.hexagonkt.helpers

import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import java.lang.System.nanoTime

/**
 * Ease the logger definition and usage. Note the logger is fetched in each call.
 *
 * TODO Check performance penalty of fetching the logger instead storing it.
 */
interface Loggable {
    fun logger (): Logger = getLogger(this::class.java)

    fun traceEnabled (): Boolean = logger().isTraceEnabled

    fun debugEnabled (): Boolean = logger().isDebugEnabled

    fun infoEnabled (): Boolean = logger().isInfoEnabled

    fun warnEnabled (): Boolean = logger().isWarnEnabled

    fun errEnabled (): Boolean = logger().isErrorEnabled

    fun flareEnabled (): Boolean = traceEnabled()

    fun timeEnabled (): Boolean = traceEnabled()

    fun trace (message: String, vararg arguments: Any) {
        logger().apply { if (isTraceEnabled) trace(message, arguments) }
    }

    fun debug (message: String, vararg arguments: Any) {
        logger().apply { if (isDebugEnabled) debug(message, arguments) }
    }

    fun info (message: String, vararg arguments: Any) {
        logger().apply { if (isInfoEnabled) info(message, arguments) }
    }

    fun warn (message: String, vararg arguments: Any) {
        logger().apply { if (isWarnEnabled) warn(message, arguments) }
    }

    fun err (message: String, vararg arguments: Any) {
        logger().apply { if (isErrorEnabled) error(message, arguments) }
    }

    fun warn (message: String, exception: Throwable) {
        logger().apply { if (isWarnEnabled) warn(message, exception) }
    }

    fun error (message: String, exception: Throwable) {
        logger().apply { if (isErrorEnabled) error(message, exception) }
    }

    fun flare (message: String = "") {
        logger().apply { if (isTraceEnabled) trace("$FLARE_PREFIX $message") }
    }

    fun time (startNanos: Long, message: String?) {
        logger().apply {
            if (isTraceEnabled)
                trace("${message ?: "TIME"} : ${formatNanos(nanoTime() - startNanos)}")
        }
    }

    fun <T> time (message: String? = null, block: () -> T): T {
        val start = nanoTime()
        return block().also { time (start, message) }
    }
}
