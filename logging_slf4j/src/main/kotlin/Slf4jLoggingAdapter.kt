package com.hexagonkt.logging

import com.hexagonkt.logging.LoggingLevel.DEBUG
import com.hexagonkt.logging.LoggingLevel.ERROR
import com.hexagonkt.logging.LoggingLevel.INFO
import com.hexagonkt.logging.LoggingLevel.TRACE
import com.hexagonkt.logging.LoggingLevel.WARN
import org.slf4j.LoggerFactory
import org.slf4j.Logger as Slf4jLogger

object Slf4jLoggingAdapter : LoggingPort {

    override fun createLogger(name: String): LoggerPort =
        object : LoggerPort {
            val log: Slf4jLogger = LoggerFactory.getLogger(name)

            override fun log(level: LoggingLevel, message: () -> Any?) {
                when (level) {
                    TRACE -> if (log.isTraceEnabled) log.trace(message().toString())
                    DEBUG -> if (log.isDebugEnabled) log.debug(message().toString())
                    INFO -> if (log.isInfoEnabled) log.info(message().toString())
                    WARN -> if (log.isWarnEnabled) log.warn(message().toString())
                    ERROR -> if (log.isErrorEnabled) log.error(message().toString())
                }
            }

            override fun <E : Throwable> log(
                level: LoggingLevel,
                exception: E,
                message: (E) -> Any?,
            ) {
                when (level) {
                    TRACE ->
                        if (log.isTraceEnabled) log.trace(message(exception).toString(), exception)
                    DEBUG ->
                        if (log.isDebugEnabled) log.debug(message(exception).toString(), exception)
                    INFO ->
                        if (log.isInfoEnabled) log.info(message(exception).toString(), exception)
                    WARN ->
                        if (log.isWarnEnabled) log.warn(message(exception).toString(), exception)
                    ERROR ->
                        if (log.isErrorEnabled) log.error(message(exception).toString(), exception)
                }
            }
        }

    override fun setLoggerLevel(name: String, level: LoggingLevel) {
        TODO("Implement for Logback")
    }
}
