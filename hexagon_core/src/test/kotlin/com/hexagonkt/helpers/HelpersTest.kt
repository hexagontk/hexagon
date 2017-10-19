package com.hexagonkt.helpers

import com.hexagonkt.serialization.JsonFormat
import com.hexagonkt.serialization.YamlFormat
import org.testng.annotations.Test
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

    fun `mime types return correct content type`() {
        assert(mimeTypes.getContentType("a.json") == JsonFormat.contentType)
        assert(mimeTypes.getContentType("a.yaml") == YamlFormat.contentType)
        assert(mimeTypes.getContentType("a.yml") == YamlFormat.contentType)
        assert(mimeTypes.getContentType("a.png") == "image/png")
        assert(mimeTypes.getContentType("a.rtf") == "application/rtf")

        assert(mimeTypes.getContentType(".json") == JsonFormat.contentType)
        assert(mimeTypes.getContentType(".yaml") == YamlFormat.contentType)
        assert(mimeTypes.getContentType(".yml") == YamlFormat.contentType)
        assert(mimeTypes.getContentType(".png") == "image/png")
        assert(mimeTypes.getContentType(".rtf") == "application/rtf")
    }

    fun `system setting works ok` () {
        System.setProperty("system_property", "value")

        assert(systemSetting("system_property") == "value")

        assert(systemSetting("PATH")?.isNotEmpty() ?: false)
        assert(systemSetting("_not_defined_") == null)

        System.setProperty("PATH", "path override")
        assert(systemSetting("PATH") == "path override")
    }

    fun `time nanos gets the elapsed nanoseconds` () {
        val nanos = System.nanoTime()
        val timeNanos = formatNanos(nanos)
        assert (timeNanos.endsWith("ms") && timeNanos.contains("."))
    }

    fun `a local date time returns a valid int timestamp` () {
        assert(dateTime (2015, 12, 31, 23, 59, 59).asNumber() == 2015_12_31_23_59_59_000)
        assert(dateTime (2015, 12, 31, 23, 59, 59, 101000000).asNumber() == 2015_12_31_23_59_59_101)
        assert(dateTime (2015, 12, 31, 23, 59, 59, 101000000).asNumber() != 2015_12_31_23_59_59_100)
    }

    fun `filtering an exception with an empty string do not change the stack` () {
        val t = RuntimeException ()
        assert (Arrays.equals (t.stackTrace, t.filterStackTrace ("")))
    }

    fun `filtering an exception with a package only returns frames of that package` () {
        val t = RuntimeException ()
        t.filterStackTrace ("com.hexagonkt").forEach {
            assert (it.className.startsWith ("com.hexagonkt"))
        }
    }

    fun `hostname and ip contains valid values` () {
        assert(hostname != "")
        assert(ip != "")
    }

    fun `printing an exception returns its stack trace in the string` () {
        val e = RuntimeException ("Runtime error")
        val trace = e.toText ()
        assert (trace.startsWith ("java.lang.RuntimeException"))
        assert (trace.contains ("\tat ${HelpersTest::class.java.name}"))
    }

    fun `printing an exception with a cause returns its stack trace in the string` () {
        val e = RuntimeException ("Runtime error", IllegalStateException ("invalid state"))
        val trace = e.toText ()
        assert (trace.startsWith ("java.lang.RuntimeException"))
        assert (trace.contains ("\tat ${HelpersTest::class.java.name}"))
    }

    fun `multiple retry errors throw an exception` () {
        val retries = 3
        try {
            retry(retries, 1, { throw RuntimeException ("Retry error") })
        }
        catch (e: CodedException) {
            assert (e.causes.size == retries)
        }
    }

    fun `retry does not allow invalid parameters` () {
        assertFailsWith<IllegalArgumentException> { retry(0, 1, { }) }
        assertFailsWith<IllegalArgumentException> { retry(1, -1, { }) }
        retry(1, 0, { }) // Ok case
    }

    fun `error utilities work as expected` () {
        assertFailsWith<IllegalStateException> { error }
    }

    fun `dates are parsed from ints`() {
        assert(2016_09_05_17_45_59_101.toLocalDateTime() ==
            LocalDateTime.of(2016, 9, 5, 17, 45, 59, 101_000_000))
        assert(2016_09_05_17_45_58_101.toLocalDateTime() !=
            LocalDateTime.of(2016, 9, 5, 17, 45, 59, 101_000_000))
    }

    fun `test get`() {
        assert(m["nested", "zulu"] == "charlie")
        assert(m["nested", "zulu", "tango"] == null)
        assert(m["nested", "empty"] == null)
        assert(m["empty"] == null)
        assert(m["alpha"] == "bravo")
        assert(m[0] == 1)
    }

    fun `test require`() {
        assert(m.require<String>("nested", "zulu") == "charlie")
        assert(m.require<String>("alpha") == "bravo")
        assert(m.require<Int>(0) == 1)
    }

    @Test(expectedExceptions = arrayOf(IllegalStateException::class))
    fun `test require not found keys`() {
        m.require<Any>("nested", "zulu", "tango")
    }

    @Test(expectedExceptions = arrayOf(IllegalStateException::class))
    fun `test require not found map`() {
        m.require<Any>("nested", "empty")
    }

    @Test(expectedExceptions = arrayOf(IllegalStateException::class))
    fun `test require not found first level`() {
        m.require<Any>("empty")
    }

    fun `date conversion`() {
        val cal = Calendar.getInstance()
        cal.set(2017, 11, 31, 0, 0, 0)
        cal.set(MILLISECOND, 0)
        val d = cal.time
        val ld = LocalDate.of(2017, 12, 31)

        assert(ld.toDate() == d)
        assert(ld == d.toLocalDate())
    }

    fun `format date`() {
        val now = LocalDateTime.now()
        assert(now.formatToIso() == now.format(ISO_DATE_TIME))
    }

    fun `zoned date`() {
        val now = LocalDateTime.now()
        assert(now.withZone(ZoneId.of("GMT")).toLocalDateTime() == now)
    }

    fun `error utilities`() {
        assertFailsWith<IllegalStateException>("Invalid state") { error }
    }

    fun `filtered maps`() {
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

    fun `filtered lists`() {
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

    fun `require resource`() {
        assert(requireResource("service_test.yaml").file == resource("service_test.yaml")?.file)
        assertFailsWith<IllegalStateException>("foo.txt not found") {
            requireResource("foo.txt")
        }
    }

    fun `resource folder`() {
        assert(resource("data")?.readText()?.lines()?.size ?: 0 > 0)
    }

    @Test(
        expectedExceptions = arrayOf(IllegalStateException::class),
        expectedExceptionsMessageRegExp = "Invalid state"
    )
    fun `error generates the correct exception`() { error }

    fun `readResource returns resource's text` () {
        val resourceText = readResource("logback-test.xml")
        assert(resourceText?.contains("Logback configuration for tests") ?:false)
    }

    fun `parse key only query parameters return correct data` () {
        assert(parseQueryParameters("a=1&b&c&d=e") == mapOf(
            "a" to "1",
            "b" to "",
            "c" to "",
            "d" to "e"
        ))
    }
}
