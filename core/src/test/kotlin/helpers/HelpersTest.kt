package com.hexagonkt.helpers

import java.net.ServerSocket
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class HelpersTest {

    private val m: Map<Any, Any> = mapOf(
        "alpha" to "bravo",
        "tango" to 0,
        "list" to listOf("first", "second"),
        "nested" to mapOf(
            "zulu" to "charlie"
        ),
        0 to 1
    )

    @Test fun `Check multiple errors`() {
        val e = assertFailsWith<MultipleException> {
            check(
                "Test multiple exceptions",
                { require(false) { "Sample error" } },
                { println("Good block")},
                { error("Bad state") },
            )
        }

        assertEquals("Test multiple exceptions", e.message)
        assertEquals(2, e.causes.size)
        assertEquals("Sample error", e.causes[0].message)
        assertEquals("Bad state", e.causes[1].message)

        check(
            "No exception thrown",
            { println("Good block")},
            { println("Shouldn't throw an exception")},
        )
    }

    @Test fun `Print helper`() {
        assertEquals("text\n", "echo text".exec().println("command output: "))
        assertEquals("text\n", "echo text".exec().println())
        assertEquals(null, null.println())
        assertEquals("text", "text".println())
    }

    @Test fun `Process execution works as expected`() {
        assertFailsWith<IllegalArgumentException> { " ".exec() }
        assertFailsWith<IllegalArgumentException> { "echo test".exec(timeout = -1) }
        assertFailsWith<IllegalArgumentException> { "echo test".exec(timeout = 0) }
        assertFailsWith<IllegalStateException> { "sleep 2".exec(timeout = 1) }
        assertFailsWith<CodedException> { "false".exec(fail = true) }

        assert("false".exec().isEmpty())
        assert("sleep 1".exec().isEmpty())
        assertEquals("str\n", "echo str".exec())
    }

    @Test fun `Network ports utilities work properly`() {
        assert(!isPortOpened(freePort()))
        ServerSocket(0).use {
            assert(isPortOpened(it.localPort))
        }
    }

    @Test fun `System setting works ok` () {
        System.setProperty("system_property", "value")

        assert(Jvm.systemSetting<String>("system_property") == "value")

        assert(Jvm.systemSetting<String>("PATH")?.isNotEmpty() ?: false)
        assert(Jvm.systemSetting<String>("_not_defined_") == null)

        System.setProperty("PATH", "path override")
        assert(Jvm.systemSetting<String>("PATH") == "path override")
    }

    @Test fun `Filtering an exception with an empty string do not change the stack` () {
        val t = RuntimeException ()
        assert (t.stackTrace?.contentEquals(t.filterStackTrace ("")) ?: false)
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
        assert(m.requireKeys<String>("nested", "zulu") == "charlie")
        assert(m.requireKeys<String>("alpha") == "bravo")
        assert(m.requireKeys<Int>(0) == 1)
    }

    @Test fun `Require not found key fails`() {
        assertFailsWith<IllegalStateException> {
            m.require("void")
        }
    }

    @Test fun `Require keys with non existing keys fails`() {
        assertFailsWith<IllegalStateException> {
            m.requireKeys("nested", "zulu", "tango")
        }
    }

    @Test fun `Require not found key in map fails`() {
        assertFailsWith<IllegalStateException> {
            m.requireKeys("nested", "empty")
        }
    }

    @Test fun `Require key not found first level throws an error`() {
        assertFailsWith<IllegalStateException> {
            m.requireKeys("empty")
        }
    }

    @Test fun `Require existing key returns correct value`() {
        assert(m.require("alpha") == "bravo")
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

    @Test fun `'fail' generates the correct exception`() {
        assertFailsWith<IllegalStateException>("Invalid state") {
            fail
        }
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

    @Test fun `Ensure fails if collection size is larger`() {
        assertFailsWith<IllegalStateException> {
            listOf(1, 2, 3).ensureSize(1..2)
        }
    }

    @Test fun `Ensure fails if collection size is smaller`() {
        assertFailsWith<IllegalStateException> {
            listOf(1, 2, 3).ensureSize(4..5)
        }
    }

    @Test fun `Ensure returns the collection if size is correct`() {
        val list = listOf(1, 2, 3)
        assert(list.ensureSize(0..3) == list)
        assert(list.ensureSize(1..3) == list)
        assert(list.ensureSize(2..3) == list)
        assert(list.ensureSize(3..3) == list)
        assert(list.ensureSize(0..4) == list)
    }
}
