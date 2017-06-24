package co.there4.hexagon.helpers

import org.testng.annotations.Test
import java.nio.file.FileSystems.newFileSystem
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.util.*
import java.util.Calendar.MILLISECOND
import java.util.Collections.emptyMap
import kotlin.test.assertFailsWith

@Test class HelpersTest {
    fun time_nanos_gets_the_elapsed_nanoseconds () {
        val nanos = System.nanoTime()
        val timeNanos = formatNanos(nanos)
        assert (timeNanos.endsWith("ms") && timeNanos.contains("."))
    }

    fun a_local_date_time_returns_a_valid_int_timestamp () {
        assert(LocalDateTime.of (2015, 12, 31, 23, 59, 59).asNumber() == 20151231235959000)
        assert(LocalDateTime.of (2015, 12, 31, 23, 59, 59, 101000000).asNumber() == 20151231235959101)
        assert(LocalDateTime.of (2015, 12, 31, 23, 59, 59, 101000000).asNumber() != 20151231235959100)
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
        assert (trace.contains ("\tat ${HelpersTest::class.java.name}"))
    }

    fun printing_an_exception_with_a_cause_returns_its_stack_trace_in_the_string () {
        val e = RuntimeException ("Runtime error", IllegalStateException ("invalid state"))
        val trace = e.toText ()
        assert (trace.startsWith ("java.lang.RuntimeException"))
        assert (trace.contains ("\tat ${HelpersTest::class.java.name}"))
    }

    fun multiple_retry_errors_throw_an_exception () {
        val retries = 3
        try {
            retry(retries, 1, { throw RuntimeException ("Retry error") })
        }
        catch (e: CodedException) {
            assert (e.causes.size == retries)
        }
    }

    fun retry_does_not_allow_invalid_parameters () {
        assertFailsWith<IllegalArgumentException> { retry(0, 1, { }) }
        assertFailsWith<IllegalArgumentException> { retry(1, -1, { }) }
        retry(1, 0, { }) // Ok case
    }

    fun error_utilities_work_as_expected () {
        assertFailsWith<IllegalStateException> { error }
    }

    fun dates_are_parsed_from_ints() {
        assert(2016_09_05_17_45_59_101.toLocalDateTime() ==
            LocalDateTime.of(2016, 9, 5, 17, 45, 59, 101_000_000))
        assert(2016_09_05_17_45_58_101.toLocalDateTime() !=
            LocalDateTime.of(2016, 9, 5, 17, 45, 59, 101_000_000))
    }

    fun testGet() {
        val m = mapOf(
            "alpha" to "bravo",
            "tango" to 0,
            "nested" to mapOf(
                "zulu" to "charlie"
            ),
            0 to 1
        )
        assert(m["nested", "zulu"] == "charlie")
        assert(m["nested", "zulu", "tango"] == null)
        assert(m["nested", "empty"] == null)
        assert(m["empty"] == null)
        assert(m["alpha"] == "bravo")
        assert(m[0] == 1)
    }

    fun date_conversion() {
        val cal = Calendar.getInstance()
        cal.set(2017, 11, 31, 0, 0, 0)
        cal.set(MILLISECOND, 0)
        val d = cal.time
        val ld = LocalDate.of(2017, 12, 31)

        assert(ld.toDate() == d)
        assert(ld == d.toLocalDate())
    }

    fun format_date() {
        val now = LocalDateTime.now()
        assert(now.formatToIso() == now.format(ISO_DATE_TIME))
    }

    fun zoned_date() {
        val now = LocalDateTime.now()
        assert(now.withZone(ZoneId.of("GMT")).toLocalDateTime() == now)
    }

    fun error_utilities() {
        assertFailsWith<IllegalStateException>("Invalid state") { error }
    }

    fun filtered_maps() {
        assert(
            mapOf(
                "a" to "b",
                "b" to null,
                "c" to 1,
                "d" to listOf(1, 2),
                "e" to listOf<String>(),
                "f" to mapOf(0 to 1),
                "g" to mapOf<String, Int>(),
                "h" to mapOf("a" to true, "b" to null).filterEmpty(),
                "i" to mapOf("a" to listOf<Int>()).filterEmpty()
            ).filterEmpty() ==
            mapOf(
                "a" to "b",
                "c" to 1,
                "d" to listOf(1, 2),
                "f" to mapOf(0 to 1),
                "h" to mapOf("a" to true)
            )
        )
    }

    fun filtered_lists() {
        assert(
            listOf(
                "a",
                null,
                listOf(1, 2),
                listOf<String>(),
                mapOf(0 to 1),
                mapOf<String, Int>(),
                mapOf("a" to true, "b" to null).filterEmpty(),
                mapOf("a" to listOf<Int>()).filterEmpty()
            ).filterEmpty() ==
            listOf(
                "a",
                listOf(1, 2),
                mapOf(0 to 1),
                mapOf("a" to true)
            )
        )
    }

    fun require_resource() {
        assert(requireResource("service_test.yaml").file == resource("service_test.yaml")?.file)
        assertFailsWith<IllegalStateException>("foo.txt not found") {
            requireResource("foo.txt")
        }
    }

    fun resource_folder() {
        assert(resource("data")?.readText()?.lines()?.size ?: 0 > 0)
    }
}
