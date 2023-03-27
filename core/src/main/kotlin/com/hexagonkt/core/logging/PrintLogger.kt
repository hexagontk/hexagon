package com.hexagonkt.core.logging

import com.hexagonkt.core.toText

data class PrintLogger(val name: String) : LoggerPort {

    override fun <E : Throwable> log(level: LoggingLevel, exception: E, message: (E) -> Any?) {
        if (LoggingManager.isLoggerLevelEnabled(name, level))
            println("$level $name - ${message(exception)}:\n${exception.toText()}")
    }

    override fun log(level: LoggingLevel, message: () -> Any?) {
        if (LoggingManager.isLoggerLevelEnabled(name, level))
            println("$level $name - ${message()}")
    }
}
