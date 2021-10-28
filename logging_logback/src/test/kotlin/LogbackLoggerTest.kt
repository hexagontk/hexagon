package com.hexagonkt.logging.logback

import com.hexagonkt.logging.Logger
import com.hexagonkt.logging.LoggingLevel
import com.hexagonkt.logging.LoggingLevel.*
import com.hexagonkt.logging.LoggingManager
import org.junit.jupiter.api.Test

internal class LogbackLoggerTest {

    /**
     * As the logger is only a facade and it is hard to check outputs, the only check is that
     * no exceptions are thrown.
     */
    @Test fun `Messages are logged without errors using Logback`() {

        LoggingManager.adapter = LogbackLoggingAdapter
        val logger = Logger(this::class)

        traceAll(logger, TRACE)
        traceAll(logger, DEBUG)
        traceAll(logger, INFO)
        traceAll(logger, WARN)
        traceAll(logger, ERROR)
        traceAll(logger, OFF)
    }

    private fun traceAll(logger: Logger, level: LoggingLevel) {
        LoggingManager.setLoggerLevel("com.hexagonkt.logging", level)
        logger.trace { 42 }
        logger.debug { true }
        logger.info { 0.0 }
        logger.warn { listOf(0, 1) }
        logger.error { mapOf(0 to 1, 2 to 3) }
        logger.warn(RuntimeException()) { 'c' }
        logger.error(RuntimeException()) { 0..100 }
        logger.flare { "message" }
        logger.time("message") {}
        logger.time {}
    }
}
