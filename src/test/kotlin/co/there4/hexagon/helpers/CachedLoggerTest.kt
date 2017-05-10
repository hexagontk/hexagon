package co.there4.hexagon.helpers

import org.testng.annotations.Test

/**
 * As the logger is only a facade and it is hard to check outputs, the only check is that
 * no exceptions are thrown.
 */
@Test class CachedLoggerTest {
    companion object : CachedLogger(CachedLoggerTest::class)

    fun messages_are_logged_without_errors () {
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
}
