package co.there4.hexagon.util

import org.testng.annotations.Test

/**
 * As the logger is only a facade and it is hard to check outputs, the only check is that
 * no exceptions are thrown.
 */
@Test class CompanionLoggerTest {
    companion object : CompanionLogger (CompanionLoggerTest::class)

    fun messages_are_logged_without_errors () {
        trace ("message")
        debug ("message")
        info ("message")
        warn ("message")
        err ("message")
        warn ("message", RuntimeException ())
        err ("message", RuntimeException ())
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
        Log.err ("message", RuntimeException ())
        Log.flare ("message")
        Log.time ("message") {}
        Log.time {}
    }
}
