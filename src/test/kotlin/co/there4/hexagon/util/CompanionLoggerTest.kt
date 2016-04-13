package co.there4.hexagon.util

import org.testng.annotations.Test

@Test class CompanionLoggerTest {
    companion object : CompanionLogger (CompanionLoggerTest::class)

    /**
     * As the logger is only a facade and it is hard to check outputs, the only check is that
     * no exceptions are thrown.
     */
    fun messages_are_logged_without_errors () {
        trace ("message")
        debug ("message")
        info ("message")
        warn ("message")
        error ("message")
        warn ("message", RuntimeException ())
        error ("message", RuntimeException ())
        flare ("message")
    }
}
