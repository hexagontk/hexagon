package com.hexagonkt.logging

import java.lang.System.nanoTime
import kotlin.reflect.KClass
import com.hexagonkt.helpers.Ansi.BLINK
import com.hexagonkt.helpers.Ansi.BOLD
import com.hexagonkt.helpers.Ansi.RESET
import com.hexagonkt.logging.LoggingLevel.*

/**
 * Logger class with Kotlin improvements like lazy evaluation. It is backed by a logging port.
 *
 * Usage example:
 *
 * @sample com.hexagonkt.HexagonCoreSamplesTest.loggerUsage
 *
 * @param name Logger name. It is shown in the logs messages and used for log filtering.
 */
class Logger(val name: String) {

    private companion object {
        const val FLARE_PREFIX = ">>>>>>>>"
    }

    internal val log: LoggerPort = LoggingManager.adapter.createLogger(name)

    /**
     * Logger class with Kotlin improvements like lazy evaluation.
     *
     * @param type Logger type. It is shown in the logs messages and used for log filtering.
     */
    constructor(type: KClass<*>):
        this(type.qualifiedName ?: error("Cannot get qualified name of $type"))

    fun trace(message: () -> Any?) {
        log.log(TRACE, message)
    }

    fun debug(message: () -> Any?) {
        log.log(DEBUG, message)
    }

    fun info(message: () -> Any?) {
        log.log(INFO, message)
    }

    fun warn(message: () -> Any?) {
        log.log(WARN, message)
    }

    fun error(message: () -> Any?) {
        log.log(ERROR, message)
    }

    fun <E : Throwable> warn(exception: E, message: (E) -> Any?) {
        log.log(WARN, exception, message)
    }

    fun <E : Throwable> error(exception: E, message: (E) -> Any?) {
        log.log(ERROR, exception, message)
    }

    fun flare(message: () -> Any? = { "" }) {
        log.log(TRACE) { "$BOLD$BLINK$FLARE_PREFIX$RESET ${message()}" }
    }

    fun time(startNanos: Long, message: () -> Any? = { "" }) {
        log.log(TRACE) { "${message() ?: "TIME"} : ${formatNanos(nanoTime() - startNanos)}" }
    }

    fun <T> time(message: () -> Any? = { null }, block: () -> T): T {
        val start = nanoTime()
        return block().also { time(start, message) }
    }

    fun <T> time(message: Any?, block: () -> T): T = this.time({ message }, block)

    private fun formatNanos (nanoseconds: Long): String = "%1.3f ms".format (nanoseconds / 1e6)
}
