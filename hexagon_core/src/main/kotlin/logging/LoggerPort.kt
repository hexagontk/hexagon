package com.hexagonkt.logging

interface LoggerPort {

    fun <E : Throwable> log(level: LoggingLevel, exception: E, message: (E) -> Any?)

    fun log(level: LoggingLevel, message: () -> Any?)
}
