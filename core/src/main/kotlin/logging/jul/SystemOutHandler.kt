package com.hexagonkt.core.logging.jul

import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.StreamHandler

/**
 * Create a StreamHandler with a given [Formatter].
 *
 * @param handlerFormatter Formatter used by the log handler.
 */
class SystemOutHandler(handlerFormatter: Formatter) : StreamHandler() {

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
