package com.hexagontk.shell

import java.io.File
import java.lang.IllegalArgumentException
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class ParameterTest {

    @Test fun `Parameters with null optional fields are correct`() {
        assertEquals(
            Parameter<String>("str", value = "val"),
            Parameter<String>("str", regex = null, value = "val")
        )
        assertEquals(
            Parameter<Boolean>("bool", value = true),
            Parameter<Boolean>("bool", description = null, value = true)
        )
    }

    @Test fun `Invalid parameters fail with exceptions`() {
        assertFailsWith<IllegalArgumentException> { Parameter<Regex>("name") }
            .message.let { assert(it?.contains("not in allowed types") ?: false) }

        setOf("", " ", "a", "Ab", "ab_c").forEach { n ->
            assertFailsWith<IllegalArgumentException> { Parameter<String>(n) }
                .message.let { assert(it?.contains("Names must comply with") ?: false) }
        }

        assertIllegalArgument("Parameter regex can only be used for 'string' type: Int") {
            Parameter<Int>("name", regex = Regex(".*"))
        }

        val e = assertFailsWith<IllegalArgumentException> {
            Parameter<String>(name = "name", regex = Regex("A"), values = listOf("a"))
        }
        assert(e.message?.contains("Value should match the 'A' regex: a") ?: false)

        assertFailsWith<IllegalArgumentException> { Parameter<Int>("name", " ") }
    }

    @Test fun `Parameters can add values`() {
        assertEquals(
            File("/foo/bar"),
            Parameter<File>("one").addValue("/foo/bar").values.first()
        )

        assertEquals(
            listOf(1, 2),
            Parameter<Int>("int", multiple = true).addValue("1").addValue("2").values
        )

        assertIllegalArgument("Parameter 'int' can only have one value: [1, 2]") {
            Parameter<Int>("int").addValue("1").addValue("2").values
        }

        assertIllegalState("Parameter 'two' of type 'URL' can not hold '0'") {
            Parameter<URL>("two").addValue("0").values.first()
        }

        assertIllegalState("Parameter 'two' of type 'Int' can not hold '/foo/bar'") {
            Parameter<Int>("two").addValue("/foo/bar").values.first()
        }
    }
}
