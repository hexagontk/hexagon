package com.hexagonkt.logging

import org.junit.jupiter.api.Test

internal class LoggerTest {

    /**
     * As the logger is only a facade and it is hard to check outputs, the only check is that
     * no exceptions are thrown.
     */
    @Test fun `Messages are logged without errors using SLF4J`() {

        LoggingManager.adapter = Slf4jLoggingAdapter
        val logger = Logger(this::class)

        logger.trace { 42 }
        logger.debug { true }
        logger.info { 0.0 }
        logger.warn { listOf(0, 1) }
        logger.error { mapOf(0 to 1, 2 to 3) }
        logger.warn(RuntimeException(), { 'c' })
        logger.error(RuntimeException(), { 0..100 })
        logger.flare { "message" }
        logger.time("message") {}
        logger.time {}
    }
}
