package com.hexagonkt.helpers

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import org.testng.annotations.Test

/**
 * As the logger is only a facade and it is hard to check outputs, the only check is that
 * no exceptions are thrown.
 */
@Test class CachedLoggerTest {
    companion object : CachedLogger(CachedLoggerTest::class)
    val l = logger() as Logger

    fun messages_are_logged_without_errors () {
        l.level = Level.TRACE

        trace ("message")
        debug ("message")
        info ("message")
        warn ("message")
        err ("message")
        warn ("message", RuntimeException ())
        error("message", RuntimeException ())
        flare ("message")
        time ("message") {}
        time {}

        l.level = Level.OFF

        trace ("message")
        debug ("message")
        info ("message")
        warn ("message")
        err ("message")
        warn ("message", RuntimeException ())
        error("message", RuntimeException ())
        flare ("message")
        time ("message") {}
        time {}
    }

    fun general_purpose_messages_are_logged_without_errors () {
        Log.trace ("message")
        Log.debug ("message")
        Log.info ("message")
        Log.warn ("message")
        Log.err ("message")
        Log.warn ("message", RuntimeException ())
        Log.error("message", RuntimeException ())
        Log.flare ("message")
        Log.time ("message") {}
        Log.time {}
    }

    fun loggable() {
        val loggable = object : Loggable {}

        loggable.trace ("message")
        loggable.debug ("message")
        loggable.info ("message")
        loggable.warn ("message")
        loggable.err ("message")
        loggable.warn ("message", RuntimeException ())
        loggable.error("message", RuntimeException ())
        loggable.flare ("message")
        loggable.time ("message") {}
        loggable.time {}
    }

    fun checkLevels() {
        val loggable = object : Loggable {}

        loggable.traceEnabled()
        loggable.debugEnabled()
        loggable.infoEnabled()
        loggable.warnEnabled()
        loggable.errEnabled()
        loggable.warnEnabled()
        loggable.flareEnabled()
        loggable.timeEnabled()
    }
}
