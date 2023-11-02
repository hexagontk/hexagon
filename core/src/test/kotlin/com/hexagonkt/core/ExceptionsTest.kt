package com.hexagonkt.core

import org.junit.jupiter.api.Test
import kotlin.test.*

internal class ExceptionsTest {

    @Test fun `Exceptions utilities`() {
        // exceptions
        // TODO
        // exceptions
    }

    @Test fun `Assure asserts are enabled in tests`() {
        assertEquals(true, assertEnabled)
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
        assert(trace.contains("\tat ${ExceptionsTest::class.java.name}"))
        assert(trace.contains("\tat org.junit.platform"))
        val filteredTrace = e.toText("com.hexagonkt")
        assert(filteredTrace.startsWith("java.lang.RuntimeException"))
        assert(filteredTrace.contains("\tat ${ExceptionsTest::class.java.name}"))
        assertFalse(filteredTrace.contains("\tat org.junit.platform"))
    }

    @Test fun `Printing an exception with a cause returns its stack trace in the string`() {
        val e = RuntimeException("Runtime error", IllegalStateException("invalid state"))
        val trace = e.toText()
        assert(trace.startsWith("java.lang.RuntimeException"))
        assert(trace.contains("invalid state"))
        assert(trace.contains("\tat ${ExceptionsTest::class.java.name}"))
        assert(trace.contains("\tat org.junit.platform"))
        val filteredTrace = e.toText("com.hexagonkt")
        assert(filteredTrace.startsWith("java.lang.RuntimeException"))
        assert(filteredTrace.contains("invalid state"))
        assert(filteredTrace.contains("\tat ${ExceptionsTest::class.java.name}"))
        assertFalse(filteredTrace.contains("\tat org.junit.platform"))
    }

    @Test fun `Check multiple errors`() {
        val e = assertFailsWith<MultipleException> {
            check(
                "Test multiple exceptions",
                { require(false) { "Sample error" } },
                { println("Good block") },
                { error("Bad state") },
            )
        }

        assertEquals("Test multiple exceptions", e.message)
        assertEquals(2, e.causes.size)
        assertEquals("Sample error", e.causes[0].message)
        assertEquals("Bad state", e.causes[1].message)

        check(
            "No exception thrown",
            { println("Good block") },
            { println("Shouldn't throw an exception") },
        )
    }
}
