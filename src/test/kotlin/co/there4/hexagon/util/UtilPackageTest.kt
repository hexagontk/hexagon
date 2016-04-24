package co.there4.hexagon.util

import org.testng.annotations.Test
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertFailsWith

@Test class UtilPackageTest {
    fun time_nanos_gets_the_elapsed_nanoseconds () {
        val millisDelay = 45L
        val millisThreshold = 5L

        resetTimes()
        pushTime()
        Thread.sleep (millisDelay)
        val nanos = popTime()
        pushTime()
        val timeNanos = formatTime(nanos)

        val ceil = (millisDelay + millisThreshold) * 1e6
        val floor = millisDelay * 1e6
        assert (nanos > floor && nanos < ceil)
        assert (timeNanos.endsWith("ms") && timeNanos.contains("."))
    }

    fun a_local_date_time_returns_a_valid_int_timestamp () {
        assert (LocalDateTime.of (2015, 12, 31, 23, 59, 59).asInt() == 20151231235959)
    }

    fun filtering_an_exception_with_an_empty_string_do_not_change_the_stack () {
        val t = RuntimeException ()
        assert (Arrays.equals (t.stackTrace, t.filterStackTrace ("")))
    }

    fun filtering_an_exception_with_a_package_only_returns_frames_of_that_package () {
        val t = RuntimeException ()
        t.filterStackTrace ("co.there4").forEach {
            assert (it.className.startsWith ("co.there4"))
        }
    }

    fun hostname_and_ip_contains_valid_values () {
        assert(hostname != UNKNOWN_LOCALHOST)
        assert(ip != UNKNOWN_LOCALHOST)
    }

    fun printing_an_exception_returns_its_stack_trace_in_the_string () {
        val e = RuntimeException ("Runtime error")
        val trace = e.toText ()
        assert (trace.startsWith ("java.lang.RuntimeException"))
        assert (trace.contains ("\tat ${UtilPackageTest::class.java.name}"))
    }

    fun printing_an_exception_with_a_cause_returns_its_stack_trace_in_the_string () {
        val e = RuntimeException ("Runtime error", IllegalStateException ("invalid state"))
        val trace = e.toText ()
        assert (trace.startsWith ("java.lang.RuntimeException"))
        assert (trace.contains ("\tat ${UtilPackageTest::class.java.name}"))
    }

    fun multiple_retry_errors_throw_an_exception () {
        val retries = 3
        try {
            retry(retries, 1, { throw RuntimeException ("Retry error") })
        }
        catch (e: ProgramException) {
            assert (e.causes.size == retries)
        }
    }

    fun retry_does_not_allow_invalid_parameters () {
        assertFailsWith<IllegalArgumentException> { retry(0, 1, { }) }
        assertFailsWith<IllegalArgumentException> { retry(1, -1, { }) }
        retry(1, 0, { }) // Ok case
    }

    fun setting_context_values_for_threads_works_correctly () {
        Context["Number"] = 9
        Context["Text"] = "Text"

        assert (Context["Number"] == 9)
        assert (Context["Text"] == "Text")
    }
}
