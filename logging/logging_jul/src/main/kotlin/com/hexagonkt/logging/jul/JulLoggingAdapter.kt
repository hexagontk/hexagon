package com.hexagonkt.logging.jul

import com.hexagonkt.core.logging.LoggerPort
import com.hexagonkt.core.logging.LoggingLevel
import com.hexagonkt.core.logging.LoggingLevel.DEBUG
import com.hexagonkt.core.logging.LoggingLevel.ERROR
import com.hexagonkt.core.logging.LoggingLevel.INFO
import com.hexagonkt.core.logging.LoggingLevel.TRACE
import com.hexagonkt.core.logging.LoggingLevel.WARN
import com.hexagonkt.core.logging.LoggingLevel.OFF
import com.hexagonkt.core.logging.LoggingManager.useColor
import com.hexagonkt.core.logging.LoggingPort
import com.hexagonkt.core.text.stripAnsi
import java.io.PrintStream
import java.util.logging.Level
import java.util.logging.Logger as JulLogger

/**
 * Implements [LoggingPort] using [Logger][JulLogger].
 */
class JulLoggingAdapter(
    messageOnly: Boolean = false,
    stream: PrintStream = System.out
) : LoggingPort {

    init {
        val root = JulLogger.getLogger("")

        for (hnd in root.handlers)
            root.removeHandler(hnd)

        val handlerFormatter = PatternFormat(useColor, messageOnly)
        val systemStreamHandler = SystemStreamHandler(handlerFormatter, stream)
        root.addHandler(systemStreamHandler)
        root.level = Level.INFO
    }

    override fun setLoggerLevel(name: String, level: LoggingLevel) {
        JulLogger.getLogger(name).level = mapLevel(level)
    }

    override fun createLogger(name: String): LoggerPort =
        object : LoggerPort {
            val log: JulLogger = JulLogger.getLogger(name)

            override fun log(level: LoggingLevel, message: () -> Any?) {
                val julLevel = mapLevel(level)
                if (log.isLoggable(julLevel))
                    log.log(julLevel, color(message().toString()))
            }

            override fun <E : Throwable> log(
                level: LoggingLevel,
                exception: E,
                message: (E) -> Any?,
            ) {
                val julLevel = mapLevel(level)
                if (log.isLoggable(julLevel))
                    log.log(julLevel, color(message(exception).toString()), exception)
            }

            private fun color(message: String): String =
                if (useColor) message
                else message.stripAnsi()
        }

    override fun isLoggerLevelEnabled(name: String, level: LoggingLevel): Boolean =
        JulLogger.getLogger(name).isLoggable(mapLevel(level))

    internal fun mapLevel(level: LoggingLevel): Level = when (level) {
        TRACE -> Level.FINER
        DEBUG -> Level.FINE
        INFO -> Level.INFO
        WARN -> Level.WARNING
        ERROR -> Level.SEVERE
        OFF -> Level.OFF
    }
}
