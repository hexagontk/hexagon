package com.hexagonkt

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.CyclicBufferAppender
import org.junit.Test

/**
 * As the logger is only a facade and it is hard to check outputs, the only check is that
 * no exceptions are thrown.
 */
class LoggingTest {

    @Test fun messages_are_logged_without_errors () {
        val log = logger as? ch.qos.logback.classic.Logger ?: com.hexagonkt.error

        log.level = Level.TRACE
        val appender = CyclicBufferAppender<ILoggingEvent>()
        appender.start()
        log.addAppender(appender)

        log.trace (42)
        assert(appender.length == 1)
        log.debug (true)
        assert(appender.length == 2)
        log.info (0.0)
        assert(appender.length == 3)
        log.warn (listOf(0, 1))
        assert(appender.length == 4)
        log.error (mapOf(0 to 1, 2 to 3))
        assert(appender.length == 5)
        log.warn ('c', RuntimeException ())
        assert(appender.length == 6)
        log.error(0..100, RuntimeException ())
        assert(appender.length == 7)
        log.flare ("message")
        assert(appender.length == 8)
        log.time ("message") {}
        assert(appender.length == 9)
        log.time {}
        assert(appender.length == 10)

        log.level = Level.OFF

        log.trace (42)
        log.debug (true)
        log.info (0.0)
        log.warn (listOf(0, 1))
        log.error (mapOf(0 to 1, 2 to 3))
        log.warn ('c', RuntimeException ())
        log.error(0..100, RuntimeException ())
        log.flare ("message")
        log.time ("message") {}
        log.time {}
        assert(appender.length == 10)
    }

    @Test fun `A logger for an instance has the proper name`() {
        assert("".logger().name == "java.lang.String")
    }
}
