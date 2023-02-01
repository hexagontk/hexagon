package com.hexagonkt.logging.slf4j.jul

import com.hexagonkt.core.logging.Logger
import com.hexagonkt.core.logging.LoggingLevel
import com.hexagonkt.core.logging.LoggingLevel.*
import com.hexagonkt.core.logging.LoggingManager
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class Slf4jJulLoggerTest {

    /**
     * As the logger is only a facade, and it is hard to check outputs, the only check is that
     * no exceptions are thrown.
     */
    @Test fun `Messages are logged without errors using SLF4J JUL`() {

        LoggingManager.adapter = Slf4jJulLoggingAdapter()
        val logger = Logger(this::class)

        traceAll(logger, TRACE)
        traceAll(logger, DEBUG)
        traceAll(logger, INFO)
        traceAll(logger, WARN)
        traceAll(logger, ERROR)
        traceAll(logger, OFF)
    }

    private fun traceAll(logger: Logger, level: LoggingLevel) {
        logger.setLoggerLevel(level)
        checkLoggerLevel(logger, level)
        logger.trace { 42 }
        logger.debug { true }
        logger.info { 0.0 }
        logger.warn { listOf(0, 1) }
        logger.error { mapOf(0 to 1, 2 to 3) }
        logger.warn(RuntimeException()) { 'c' }
        logger.error(RuntimeException()) { 0..100 }
        logger.flare { "message" }
    }

    private fun checkLoggerLevel(logger: Logger, level: LoggingLevel) {
        assertTrue(level == OFF || logger.isLoggerLevelEnabled(level))

        when (level) {
            TRACE -> {
                assertTrue(logger.isTraceEnabled())
                assertTrue(logger.isDebugEnabled())
                assertTrue(logger.isInfoEnabled())
                assertTrue(logger.isWarnEnabled())
                assertTrue(logger.isErrorEnabled())
            }

            DEBUG -> {
                assertFalse(logger.isTraceEnabled())
                assertTrue(logger.isDebugEnabled())
                assertTrue(logger.isInfoEnabled())
                assertTrue(logger.isWarnEnabled())
                assertTrue(logger.isErrorEnabled())
            }

            INFO -> {
                assertFalse(logger.isTraceEnabled())
                assertFalse(logger.isDebugEnabled())
                assertTrue(logger.isInfoEnabled())
                assertTrue(logger.isWarnEnabled())
                assertTrue(logger.isErrorEnabled())
            }

            WARN -> {
                assertFalse(logger.isTraceEnabled())
                assertFalse(logger.isDebugEnabled())
                assertFalse(logger.isInfoEnabled())
                assertTrue(logger.isWarnEnabled())
                assertTrue(logger.isErrorEnabled())
            }

            ERROR -> {
                assertFalse(logger.isTraceEnabled())
                assertFalse(logger.isDebugEnabled())
                assertFalse(logger.isInfoEnabled())
                assertFalse(logger.isWarnEnabled())
                assertTrue(logger.isErrorEnabled())
            }

            OFF -> {
                assertFalse(logger.isTraceEnabled())
                assertFalse(logger.isDebugEnabled())
                assertFalse(logger.isInfoEnabled())
                assertFalse(logger.isWarnEnabled())
                assertFalse(logger.isErrorEnabled())
            }
        }
    }
}
