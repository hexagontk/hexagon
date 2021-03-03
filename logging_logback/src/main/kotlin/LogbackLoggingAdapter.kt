package com.hexagonkt.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.hexagonkt.logging.LoggingLevel.*
import org.slf4j.LoggerFactory

object LogbackLoggingAdapter : LoggingPort {

    override fun createLogger(name: String): LoggerPort =
        Slf4jJulLoggingAdapter.createLogger(name)

    override fun setLoggerLevel(name: String, level: LoggingLevel) {
        (LoggerFactory.getLogger(name) as Logger).level = mapLevel(level)
    }

    private fun mapLevel(level: LoggingLevel): Level = when (level) {
        TRACE -> Level.TRACE
        DEBUG -> Level.DEBUG
        INFO -> Level.INFO
        WARN -> Level.WARN
        ERROR -> Level.ERROR
    }
}
