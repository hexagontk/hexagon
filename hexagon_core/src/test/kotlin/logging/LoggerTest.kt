package com.hexagonkt.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.CyclicBufferAppender
import com.hexagonkt.helpers.fail
import com.hexagonkt.helpers.logger
import org.junit.jupiter.api.Test

/**
 * As the logger is only a facade and it is hard to check outputs, the only check is that
 * no exceptions are thrown.
 */
class LoggerTest {

    @Test fun `Messages are logged without errors`() {
        val log = logger.log as? ch.qos.logback.classic.Logger ?: fail

        log.level = Level.TRACE
        val appender = CyclicBufferAppender<ILoggingEvent>()
        appender.start()
        log.addAppender(appender)

        logger.trace { 42 }
        assert(appender.length == 1)
        logger.debug { true }
        assert(appender.length == 2)
        logger.info { 0.0 }
        assert(appender.length == 3)
        logger.warn { listOf(0, 1) }
        assert(appender.length == 4)
        logger.error { mapOf(0 to 1, 2 to 3) }
        assert(appender.length == 5)
        logger.warn(RuntimeException()) { 'c' }
        assert(appender.length == 6)
        logger.error(RuntimeException()) { 0..100 }
        assert(appender.length == 7)
        logger.flare { "message" }
        assert(appender.length == 8)
        logger.flare()
        assert(appender.length == 9)
        logger.time("message") {}
        assert(appender.length == 10)
        logger.time {}
        assert(appender.length == 11)
        logger.time(0)
        assert(appender.length == 12)

        log.level = Level.OFF

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
        assert(appender.length == 12)
    }

    @Test fun `A logger for an instance has the proper name`() {
        assert(Logger("").log.name == "java.lang.String")
    }
}
