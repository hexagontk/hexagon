package com.hexagonkt.core.logging

import com.hexagonkt.core.logging.LoggingLevel.*

data class SystemLogger(val name: String) : LoggerPort {

    private val logger: System.Logger = System.getLogger(name)

    override fun <E : Throwable> log(level: LoggingLevel, exception: E, message: (E) -> Any?) {
        if (LoggingManager.isLoggerLevelEnabled(name, level))
            logger.log(level(level), message(exception).toString(), exception)
    }

    override fun log(level: LoggingLevel, message: () -> Any?) {
        if (LoggingManager.isLoggerLevelEnabled(name, level))
            logger.log(level(level), message())
    }

    private fun level(level: LoggingLevel): System.Logger.Level =
        when (level) {
            TRACE -> System.Logger.Level.TRACE
            DEBUG -> System.Logger.Level.DEBUG
            INFO -> System.Logger.Level.INFO
            WARN -> System.Logger.Level.WARNING
            ERROR -> System.Logger.Level.ERROR
            OFF -> System.Logger.Level.OFF
        }
}
