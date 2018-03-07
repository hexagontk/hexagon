package com.hexagonkt.helpers

import org.slf4j.Logger
import java.lang.System.nanoTime

/**
 * Ease the logger definition and usage. Note the logger is fetched in each call.
 */
interface Loggable {
    val log: Logger

    fun trace (message: Any?) { if (log.isTraceEnabled) log.trace(message.toString()) }

    fun debug (message: Any?) { if (log.isDebugEnabled) log.debug(message.toString()) }

    fun info (message: Any?) { if (log.isInfoEnabled) log.info(message.toString()) }

    fun warn (message: Any?) { if (log.isWarnEnabled) log.warn(message.toString()) }

    fun fail (message: Any?) { if (log.isErrorEnabled) log.error(message.toString()) }

    fun warn (message: Any?, exception: Throwable) {
        if (log.isWarnEnabled) log.warn(message.toString(), exception)
    }

    fun fail (message: Any?, exception: Throwable) {
        if (log.isErrorEnabled) log.error(message.toString(), exception)
    }

    fun flare (message: Any? = "") {
        if (log.isTraceEnabled) trace("$FLARE_PREFIX $message")
    }

    fun time (startNanos: Long, message: Any?) {
        if (log.isTraceEnabled)
            log.trace("${message ?: "TIME"} : ${formatNanos(nanoTime() - startNanos)}")
    }

    fun <T> time (message: Any? = null, block: () -> T): T {
        val start = nanoTime()
        return block().also { time (start, message) }
    }
}
