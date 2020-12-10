package com.hexagonkt.logging.jul

import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.StreamHandler

class SystemOutHandler(handlerFormatter: Formatter = PatternFormat()) : StreamHandler() {

    override fun publish(record: LogRecord) {
        super.publish(record)
        flush()
    }

    init {
        setOutputStream(System.out)
        formatter = handlerFormatter
        level = Level.ALL
    }
}
