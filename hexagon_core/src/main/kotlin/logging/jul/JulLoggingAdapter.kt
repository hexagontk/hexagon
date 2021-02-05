package com.hexagonkt.logging.jul

import com.hexagonkt.logging.LoggerPort
import com.hexagonkt.logging.LoggingLevel
import com.hexagonkt.logging.LoggingLevel.*
import com.hexagonkt.logging.LoggingPort
import java.util.logging.*
import java.util.logging.Logger as JulLogger

object JulLoggingAdapter : LoggingPort {

    private val root = JulLogger.getLogger("")

    init {
        for (hnd in root.handlers)
            root.removeHandler(hnd)

        root.addHandler(SystemOutHandler())
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

    private fun mapLevel(level: LoggingLevel) = when (level) {
        TRACE -> Level.FINER
        DEBUG -> Level.FINE
        INFO -> Level.INFO
        WARN -> Level.WARNING
        ERROR -> Level.SEVERE
    }
}
