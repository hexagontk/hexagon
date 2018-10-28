package com.hexagonkt.helpers

import org.slf4j.LoggerFactory.getLogger
import java.lang.System.nanoTime
import kotlin.reflect.KClass
import org.slf4j.Logger as Slf4jLogger

class Logger(internal val log: Slf4jLogger) {

    companion object {
        internal const val FLARE_PREFIX = ">>>>>>>>"

        fun of(type: KClass<*>): Logger = Logger(getLogger(type.java))
    }

    fun trace(message: () -> Any?) {
        if (log.isTraceEnabled) log.trace(message().toString())
    }

    fun debug(message: () -> Any?) {
        if (log.isDebugEnabled) log.debug(message().toString())
    }

    fun info(message: () -> Any?) {
        if (log.isInfoEnabled) log.info(message().toString())
    }

    fun warn(message: () -> Any?) {
        if (log.isWarnEnabled) log.warn(message().toString())
    }

    fun error(message: () -> Any?) {
        if (log.isErrorEnabled) log.error(message().toString())
    }

    fun warn(message: () -> Any?, exception: Throwable) {
        if (log.isWarnEnabled) log.warn(message().toString(), exception)
    }

    fun error(message: () -> Any?, exception: Throwable) {
        if (log.isErrorEnabled) log.error(message().toString(), exception)
    }

    fun flare(message: () -> Any? = { "" }) {
        if (log.isTraceEnabled) log.trace("$FLARE_PREFIX ${message()}")
    }

    fun time(startNanos: Long, message: () -> Any? = { "" }) {
        if (log.isTraceEnabled)
            log.trace("${message() ?: "TIME"} : ${formatNanos(nanoTime() - startNanos)}")
    }

    fun <T> time(message: () -> Any? = { null }, block: () -> T): T {
        val start = nanoTime()
        return block().also { time(start, message) }
    }

    fun <T> time(message: Any?, block: () -> T): T = this.time({ message }, block)
}
