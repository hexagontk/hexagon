package com.hexagonkt.core

import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS.WINDOWS
import kotlin.test.Test
import kotlin.test.*

internal class HelpersTest {

    @Test fun `Properties can be loaded from URLs`() {
        val properties = properties(urlOf("classpath:build.properties"))
        assertEquals("hexagon", properties["project"])
        assertEquals("core", properties["module"])
        assertEquals("1.0.0", properties["version"])
        assertEquals("com.hexagonkt", properties["group"])
        assertEquals("Hexagon Toolkit", properties["description"])
        assertEquals("1", properties["number"])
        assertNull(properties["invalid"])

        assertFailsWith<ResourceNotFoundException> {
            properties(urlOf("classpath:invalid.properties"))
        }
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

    @Test
    @DisabledOnOs(WINDOWS)
    fun `Shell execution works as expected`() {
        assertEquals("test", "echo test".shell().trim())
    }

    @Test fun `System setting works ok`() {
        System.setProperty("system_property", "value")

        assert(Jvm.systemSetting<String>("system_property") == "value")

        assert(Jvm.systemSetting<String>("PATH").isNotEmpty())
        assertNull(Jvm.systemSettingOrNull<String>("_not_defined_"))

        System.setProperty("PATH", "path override")
        assert(Jvm.systemSetting<String>("PATH") == "path override")
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
