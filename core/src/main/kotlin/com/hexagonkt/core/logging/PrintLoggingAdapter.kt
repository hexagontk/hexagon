package com.hexagonkt.core.logging

import com.hexagonkt.core.logging.LoggingLevel.INFO
import com.hexagonkt.core.require

// TODO Wrap these loggers using System.Logger:
//  https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/System.Logger.html
class PrintLoggingAdapter(defaultLevel: LoggingLevel = INFO) : LoggingPort {

    private val loggerLevels: MutableMap<String, LoggingLevel> = mutableMapOf("" to defaultLevel)

    override fun createLogger(name: String): LoggerPort =
        PrintLogger(name)

    override fun setLoggerLevel(name: String, level: LoggingLevel) {
        loggerLevels[name] = level
    }

    override fun isLoggerLevelEnabled(name: String, level: LoggingLevel): Boolean =
        findLoggingLevel(name).ordinal <= level.ordinal

    private fun findLoggingLevel(name: String): LoggingLevel {
        var path = name

        do {
            val loggingLevel = loggerLevels[path]
            if (loggingLevel != null)
                return loggingLevel

            path = path.substringBeforeLast('.')
        }
        while (path.contains('.'))

        return loggerLevels.require("")
    }
}
