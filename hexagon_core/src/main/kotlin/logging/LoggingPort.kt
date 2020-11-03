package com.hexagonkt.logging

interface LoggingPort {

    fun createLogger(name: String): LoggerPort

//    fun setLoggerLevel(name: String, level: LoggingLevel) // TODO
}
