package com.hexagonkt.logging.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.hexagonkt.core.logging.LoggerPort
import com.hexagonkt.core.logging.LoggingLevel
import com.hexagonkt.core.logging.LoggingLevel.DEBUG
import com.hexagonkt.core.logging.LoggingLevel.ERROR
import com.hexagonkt.core.logging.LoggingLevel.INFO
import com.hexagonkt.core.logging.LoggingLevel.TRACE
import com.hexagonkt.core.logging.LoggingLevel.WARN
import com.hexagonkt.core.logging.LoggingLevel.OFF
import com.hexagonkt.core.logging.LoggingManager
import com.hexagonkt.core.logging.LoggingPort
import com.hexagonkt.core.text.stripAnsi
import org.slf4j.LoggerFactory

class LogbackLoggingAdapter : LoggingPort {

    override fun createLogger(name: String): LoggerPort =
        object : LoggerPort {
            val log: org.slf4j.Logger = LoggerFactory.getLogger(name)

            override fun log(level: LoggingLevel, message: () -> Any?) {
                val processedMessage = color(message().toString())
                when (level) {
                    TRACE -> if (log.isTraceEnabled) log.trace(processedMessage)
                    DEBUG -> if (log.isDebugEnabled) log.debug(processedMessage)
                    INFO -> if (log.isInfoEnabled) log.info(processedMessage)
                    WARN -> if (log.isWarnEnabled) log.warn(processedMessage)
                    ERROR -> if (log.isErrorEnabled) log.error(processedMessage)
                    OFF -> {}
                }
            }

            override fun <E : Throwable> log(
                level: LoggingLevel,
                exception: E,
                message: (E) -> Any?,
            ) {
                val processedMessage = color(message(exception).toString())
                when (level) {
                    WARN -> if (log.isWarnEnabled) log.warn(processedMessage, exception)
                    ERROR -> if (log.isErrorEnabled) log.error(processedMessage, exception)
                    else -> {}
                }
            }

            private fun color(message: String): String =
                if (LoggingManager.useColor) message
                else message.stripAnsi()
        }

    override fun setLoggerLevel(name: String, level: LoggingLevel) {
        val loggerName = name.ifEmpty { Logger.ROOT_LOGGER_NAME }
        (LoggerFactory.getLogger(loggerName) as Logger).level = mapLevel(level)
    }

    override fun isLoggerLevelEnabled(name: String, level: LoggingLevel): Boolean {
        val loggerName = name.ifEmpty { Logger.ROOT_LOGGER_NAME }
        return (LoggerFactory.getLogger(loggerName) as Logger).isEnabledFor(mapLevel(level))
    }

    private fun mapLevel(level: LoggingLevel): Level = when (level) {
        TRACE -> Level.TRACE
        DEBUG -> Level.DEBUG
        INFO -> Level.INFO
        WARN -> Level.WARN
        ERROR -> Level.ERROR
        OFF -> Level.OFF
    }
}
