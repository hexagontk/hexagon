package com.hexagonkt.helpers

import ch.qos.logback.classic.Level
import org.slf4j.Logger
import org.testng.annotations.Test

/**
 * As the logger is only a facade and it is hard to check outputs, the only check is that
 * no exceptions are thrown.
 */
@Test class LoggableTest {
    object LoggableObject : Loggable {
        override val log: Logger = loggerOf<LoggableTest>()
    }

    fun messages_are_logged_without_errors () {
        if(LoggableObject.log !is ch.qos.logback.classic.Logger)
            error

        LoggableObject.log.level = Level.TRACE

        LoggableObject.trace ("message")
        LoggableObject.debug ("message")
        LoggableObject.info ("message")
        LoggableObject.warn ("message")
        LoggableObject.fail ("message")
        LoggableObject.warn ("message", RuntimeException ())
        LoggableObject.fail("message", RuntimeException ())
        LoggableObject.flare ("message")
        LoggableObject.time ("message") {}
        LoggableObject.time {}

        LoggableObject.log.level = Level.OFF

        LoggableObject.trace ("message")
        LoggableObject.debug ("message")
        LoggableObject.info ("message")
        LoggableObject.warn ("message")
        LoggableObject.fail ("message")
        LoggableObject.warn ("message", RuntimeException ())
        LoggableObject.fail("message", RuntimeException ())
        LoggableObject.flare ("message")
        LoggableObject.time ("message") {}
        LoggableObject.time {}
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
