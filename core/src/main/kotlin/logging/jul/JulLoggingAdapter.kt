package com.hexagonkt.core.logging.jul

import com.hexagonkt.core.logging.LoggerPort
import com.hexagonkt.core.logging.LoggingLevel
import com.hexagonkt.core.logging.LoggingLevel.DEBUG
import com.hexagonkt.core.logging.LoggingLevel.ERROR
import com.hexagonkt.core.logging.LoggingLevel.INFO
import com.hexagonkt.core.logging.LoggingLevel.TRACE
import com.hexagonkt.core.logging.LoggingLevel.WARN
import com.hexagonkt.core.logging.LoggingLevel.OFF
import com.hexagonkt.core.logging.LoggingPort
import java.util.logging.Level
import java.util.logging.Logger as JulLogger

/**
 * Implements [LoggingPort] using [Logger][JulLogger].
 */
class JulLoggingAdapter(useColor: Boolean = true) : LoggingPort {

    init {
        val root = JulLogger.getLogger("")

        for (hnd in root.handlers)
            root.removeHandler(hnd)

        root.addHandler(SystemOutHandler(PatternFormat(useColor)))
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
                    log.log(julLevel, message().toString())
            }

            override fun <E : Throwable> log(
                level: LoggingLevel,
                exception: E,
                message: (E) -> Any?,
            ) {
                val julLevel = mapLevel(level)
                if (log.isLoggable(julLevel))
                    log.log(julLevel, message(exception).toString(), exception)
            }
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
