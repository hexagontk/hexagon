package com.hexagonkt.helpers

import ch.qos.logback.classic.Level
import org.slf4j.Logger
import org.testng.annotations.Test

/**
 * As the logger is only a facade and it is hard to check outputs, the only check is that
 * no exceptions are thrown.
 */
@Test class LoggableTest : Loggable {
    override val log: Logger = loggerOf<LoggableTest>()

    fun messages_are_logged_without_errors () {
        if(log !is ch.qos.logback.classic.Logger)
            error

        log.level = Level.TRACE

        trace ("message")
        debug ("message")
        info ("message")
        warn ("message")
        fail ("message")
        warn ("message", RuntimeException ())
        fail("message", RuntimeException ())
        flare ("message")
        time ("message") {}
        time {}

        log.level = Level.OFF

        trace ("message")
        debug ("message")
        info ("message")
        warn ("message")
        fail ("message")
        warn ("message", RuntimeException ())
        fail("message", RuntimeException ())
        flare ("message")
        time ("message") {}
        time {}
    }

    fun general_purpose_messages_are_logged_without_errors () {
        Log.trace ("message")
        Log.debug ("message")
        Log.info ("message")
        Log.warn ("message")
        Log.fail ("message")
        Log.warn ("message", RuntimeException ())
        Log.fail("message", RuntimeException ())
        Log.flare ("message")
        Log.time ("message") {}
        Log.time {}
    }
}
