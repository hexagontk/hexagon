package com.hexagonkt.logging.jul

import java.io.PrintStream
import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.StreamHandler

/**
 * Create a StreamHandler with a given [Formatter].
 *
 * @param handlerFormatter Formatter used by the log handler.
 */
internal class SystemStreamHandler(
    handlerFormatter: Formatter,
    stream: PrintStream = System.out
) : StreamHandler() {

    override fun publish(record: LogRecord) {
        super.publish(record)
        try {
            flush()
        }
        catch (_: Exception) {
            // TODO This is done because of a weird error starting Nima (try removing with JDK 21)
        }
    }

    init {
        setOutputStream(stream)
        formatter = handlerFormatter
        level = Level.ALL
    }
}
