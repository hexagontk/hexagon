package com.hexagonkt.core

import kotlin.test.Test
import java.net.URL
import kotlin.test.*

internal class HelpersTest {

    @Test fun `Properties can be loaded from URLs`() {
        val properties = properties(URL("classpath:build.properties"))
        assertEquals("hexagon", properties["project"])
        assertEquals("core", properties["module"])
        assertEquals("1.0.0", properties["version"])
        assertEquals("com.hexagonkt", properties["group"])
        assertEquals("Hexagon Toolkit", properties["description"])
        assertEquals("1", properties["number"])
        assertNull(properties["invalid"])

        assertFailsWith<ResourceNotFoundException> {
            properties(URL("classpath:invalid.properties"))
        }
    }

    @Test fun `Assure asserts are enabled in tests`() {
        assertEquals(true, assertEnabled)
    }

    @Test fun `Check multiple errors`() {
        val e = assertFailsWith<MultipleException> {
            check(
                "Test multiple exceptions",
                { require(false) { "Sample error" } },
                { out("Good block") },
                { error("Bad state") },
            )
        }

        assertEquals("Test multiple exceptions", e.message)
        assertEquals(2, e.causes.size)
        assertEquals("Sample error", e.causes[0].message)
        assertEquals("Bad state", e.causes[1].message)

        check(
            "No exception thrown",
            { out("Good block") },
            { out("Shouldn't throw an exception") },
        )
    }

    @Test fun `Print stdout helper`() {
        assertEquals("text\n", "echo text".exec().out("command output: "))
        assertEquals("text\n", "echo text".exec().out())
        assertEquals(null, null.out())
        assertEquals("text", "text".out())
    }

    @Test fun `Print stderr helper`() {
        assertEquals("text\n", "echo text".exec().err("command output: "))
        assertEquals("text\n", "echo text".exec().err())
        assertEquals(null, null.err())
        assertEquals("text", "text".err())
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

    @Test fun `System setting works ok`() {
        System.setProperty("system_property", "value")

        assert(Jvm.systemSetting<String>("system_property") == "value")

        assert(Jvm.systemSetting<String>("PATH").isNotEmpty())
        assertNull(Jvm.systemSettingOrNull<String>("_not_defined_"))

        System.setProperty("PATH", "path override")
        assert(Jvm.systemSetting<String>("PATH") == "path override")
    }

    @Test fun `Filtering an exception with an empty string do not change the stack`() {
        val t = RuntimeException()
        assert(t.stackTrace?.contentEquals(t.filterStackTrace("")) ?: false)
    }

    @Test fun `Filtering an exception with a package only returns frames of that package`() {
        val t = RuntimeException()
        t.filterStackTrace("com.hexagonkt.core").forEach {
            assert(it.className.startsWith("com.hexagonkt.core"))
        }
    }

    @Test fun `'fail' generates the correct exception`() {
        assertFailsWith<IllegalStateException>("Invalid state") {
            fail
        }
    }

    @Test fun `Printing an exception returns its stack trace in the string`() {
        val e = RuntimeException("Runtime error")
        val trace = e.toText()
        assert(trace.startsWith("java.lang.RuntimeException"))
        assert(trace.contains("\tat ${HelpersTest::class.java.name}"))
        assert(trace.contains("\tat org.junit.platform"))
        val filteredTrace = e.toText("com.hexagonkt")
        assert(filteredTrace.startsWith("java.lang.RuntimeException"))
        assert(filteredTrace.contains("\tat ${HelpersTest::class.java.name}"))
        assertFalse(filteredTrace.contains("\tat org.junit.platform"))
    }

    @Test fun `Printing an exception with a cause returns its stack trace in the string`() {
        val e = RuntimeException("Runtime error", IllegalStateException("invalid state"))
        val trace = e.toText()
        assert(trace.startsWith("java.lang.RuntimeException"))
        assert(trace.contains("invalid state"))
        assert(trace.contains("\tat ${HelpersTest::class.java.name}"))
        assert(trace.contains("\tat org.junit.platform"))
        val filteredTrace = e.toText("com.hexagonkt")
        assert(filteredTrace.startsWith("java.lang.RuntimeException"))
        assert(filteredTrace.contains("invalid state"))
        assert(filteredTrace.contains("\tat ${HelpersTest::class.java.name}"))
        assertFalse(filteredTrace.contains("\tat org.junit.platform"))
    }

    @Test fun `Multiple retry errors throw an exception`() {
        val retries = 3
        try {
            retry(retries, 1) { throw RuntimeException("Retry error") }
        }
        catch (e: MultipleException) {
            assertEquals(retries, e.causes.size)
        }
    }

    @Test fun `Retry does not allow invalid parameters`() {
        assertFailsWith<IllegalArgumentException> { retry(0, 1) {} }
        assertFailsWith<IllegalArgumentException> { retry(1, -1) {} }
        retry(1, 0) {} // Ok case
    }
}
