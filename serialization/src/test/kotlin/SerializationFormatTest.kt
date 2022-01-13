package com.hexagonkt.serialization

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class SerializationFormatTest {

    @Test fun `A binary format fails to serialize to a string`() {
        assertFailsWith<IllegalStateException> {
            BinaryTestFormat.serialize("foo")
        }
    }

    @Test fun `A text format can be serialized to a string`() {
        assertEquals("foo", TextTestFormat.serialize("foo"))
    }

    @Test fun `Text formats handle errors properly on parse`() {
        assertEquals(listOf("text"), TextTestFormat.parse(""))
        assertFailsWith<java.lang.IllegalStateException> { BinaryTestFormat.parse("") }
    }
}
