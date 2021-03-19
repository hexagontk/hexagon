package com.hexagonkt.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.hexagonkt.logging.LoggingLevel.DEBUG
import com.hexagonkt.logging.LoggingLevel.ERROR
import com.hexagonkt.logging.LoggingLevel.INFO
import com.hexagonkt.logging.LoggingLevel.TRACE
import com.hexagonkt.logging.LoggingLevel.WARN
import org.slf4j.LoggerFactory

object LogbackLoggingAdapter : LoggingPort {

    override fun createLogger(name: String): LoggerPort =
        Slf4jJulLoggingAdapter.createLogger(name)

    override fun setLoggerLevel(name: String, level: LoggingLevel) {
        val loggerName = name.ifEmpty { Logger.ROOT_LOGGER_NAME }
        (LoggerFactory.getLogger(loggerName) as Logger).level = mapLevel(level)
    }

    private fun mapLevel(level: LoggingLevel): Level = when (level) {
        TRACE -> Level.TRACE
        DEBUG -> Level.DEBUG
        INFO -> Level.INFO
        WARN -> Level.WARN
        ERROR -> Level.ERROR
    }
}
