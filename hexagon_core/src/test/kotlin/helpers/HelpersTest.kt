package com.hexagonkt.helpers

import org.testng.annotations.Test
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.of as dateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.util.*
import java.util.Calendar.MILLISECOND
import kotlin.test.assertFailsWith

@Test class HelpersTest {
    private val m = mapOf(
        "alpha" to "bravo",
        "tango" to 0,
        "nested" to mapOf(
            "zulu" to "charlie"
        ),
        0 to 1
    )

    @Test fun `System setting works ok` () {
        System.setProperty("system_property", "value")

        assert(Jvm.systemSetting("system_property") == "value")

        assert(Jvm.systemSetting("PATH")?.isNotEmpty() ?: false)
        assert(Jvm.systemSetting("_not_defined_") == null)

        System.setProperty("PATH", "path override")
        assert(Jvm.systemSetting("PATH") == "path override")
    }

    @Test fun `Time nanos gets the elapsed nanoseconds` () {
        val nanos = System.nanoTime()
        val timeNanos = formatNanos(nanos)

        val decimalSeparator = DecimalFormat().decimalFormatSymbols.decimalSeparator
        assert (timeNanos.endsWith("ms") && timeNanos.contains(decimalSeparator))
    }

    @Test fun `A local date time returns a valid int timestamp` () {
        assert(dateTime (2015, 12, 31, 23, 59, 59).toNumber() == 2015_12_31_23_59_59_000)
        assert(dateTime (2015, 12, 31, 23, 59, 59, 101000000).toNumber() == 2015_12_31_23_59_59_101)
        assert(dateTime (2015, 12, 31, 23, 59, 59, 101000000).toNumber() != 2015_12_31_23_59_59_100)
    }

    @Test fun `Filtering an exception with an empty string do not change the stack` () {
        val t = RuntimeException ()
        assert (Arrays.equals (t.stackTrace, t.filterStackTrace ("")))
    }

    @Test fun `Filtering an exception with a package only returns frames of that package` () {
        val t = RuntimeException ()
        t.filterStackTrace ("com.hexagonkt").forEach {
            assert (it.className.startsWith ("com.hexagonkt"))
        }
    }

    @Test fun `Get nested keys inside a map returns the proper value`() {
        assert(m["nested", "zulu"] == "charlie")
        assert(m["nested", "zulu", "tango"] == null)
        assert(m["nested", "empty"] == null)
        assert(m["empty"] == null)
        assert(m["alpha"] == "bravo")
        assert(m[0] == 1)
    }

    @Test fun `Require a value defined by a list of keys return the correct value`() {
        assert(m.require<String>("nested", "zulu") == "charlie")
        assert(m.require<String>("alpha") == "bravo")
        assert(m.require<Int>(0) == 1)
    }

    @Test(expectedExceptions = [ IllegalStateException::class ])
    fun `Require not found keys fails`() {
        m.require<Any>("nested", "zulu", "tango")
    }

    @Test(expectedExceptions = [ IllegalStateException::class ])
    fun `Require not found key in map fails`() {
        m.require<Any>("nested", "empty")
    }

    @Test(expectedExceptions = [ IllegalStateException::class ])
    fun `Require key not found first level throws an error`() {
        m.require<Any>("empty")
    }

    @Test fun `Date conversion`() {
        val cal = Calendar.getInstance()
        cal.set(2017, 11, 31, 0, 0, 0)
        cal.set(MILLISECOND, 0)
        val d = cal.time
        val ld = LocalDate.of(2017, 12, 31)

        assert(ld.toDate() == d)
        assert(ld == d.toLocalDate())
    }

    @Test fun `Format date`() {
        val now = LocalDateTime.now()
        assert(now.formatToIso() == now.format(ISO_DATE_TIME))
    }

    @Test fun `Zoned date`() {
        val now = LocalDateTime.now()
        assert(now.withZone(ZoneId.of("GMT")).toLocalDateTime() == now)
    }

    @Test fun `Filtered maps do not contain empty elements`() {
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

    @Test fun `Filtered lists do not contain empty elements`() {
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

    @Test(
        expectedExceptions = [ IllegalStateException::class ],
        expectedExceptionsMessageRegExp = "Invalid state"
    )
    fun `'error' generates the correct exception`() {
        error()
    }

    @Test fun `Printing an exception returns its stack trace in the string` () {
        val e = RuntimeException ("Runtime error")
        val trace = e.toText ()
        assert (trace.startsWith ("java.lang.RuntimeException"))
        assert (trace.contains ("\tat ${HelpersTest::class.java.name}"))
    }

    @Test fun `Printing an exception with a cause returns its stack trace in the string` () {
        val e = RuntimeException ("Runtime error", IllegalStateException ("invalid state"))
        val trace = e.toText ()
        assert (trace.startsWith ("java.lang.RuntimeException"))
        assert (trace.contains ("\tat ${HelpersTest::class.java.name}"))
    }

    @Test fun `Multiple retry errors throw an exception` () {
        val retries = 3
        try {
            retry(retries, 1) { throw RuntimeException ("Retry error") }
        }
        catch (e: MultipleException) {
            assert (e.causes.size == retries)
        }
    }

    @Test fun `Retry does not allow invalid parameters` () {
        assertFailsWith<IllegalArgumentException> { retry(0, 1) {} }
        assertFailsWith<IllegalArgumentException> { retry(1, -1) {} }
        retry(1, 0) {} // Ok case
    }

    @Test fun `Dates are parsed from ints`() {
        assert(2016_09_05_17_45_59_101.toLocalDateTime() ==
            LocalDateTime.of(2016, 9, 5, 17, 45, 59, 101_000_000))
        assert(2016_09_05_17_45_58_101.toLocalDateTime() !=
            LocalDateTime.of(2016, 9, 5, 17, 45, 59, 101_000_000))
    }
}
