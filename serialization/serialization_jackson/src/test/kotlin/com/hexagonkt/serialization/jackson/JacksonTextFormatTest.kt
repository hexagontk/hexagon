package com.hexagonkt.serialization.jackson

import com.hexagonkt.core.media.APPLICATION_JSON
import com.hexagonkt.core.media.MediaType
import kotlin.test.Test
import org.junit.jupiter.api.TestInstance
import kotlin.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class JacksonTextFormatTest {

    object TextFormat : JacksonTextFormat() {
        override val mediaType: MediaType = APPLICATION_JSON
    }

    @Test fun `Parse objects to nodes`() {
        assertEquals(emptyList<Any>(), TextFormat.parse("[]"))
        assertEquals(emptyMap<Any, Any>(), TextFormat.parse("{}"))

        val expected = mapOf(
            "array" to listOf(1, 2, 3),
            "object" to mapOf(
                "text" to "text",
                "boolean" to true,
                "double" to 0.123456789,
            ),
            "long" to 1_234_567_890_123_456_789L,
            "null" to null,
        )
        val json = """
            {
                "array" : [ 1, 2, 3 ],
                "object" : {
                    "text" : "text",
                    "boolean" : true,
                    "double" : 0.123456789,
                },
                "long" : 1234567890123456789,
                "null" : null,
            }
        """

        assertEquals(expected, TextFormat.parse(json))
    }

    @Test fun `Errors parsing input are handled`() {
        assertEquals(
            "Unknown node type: com.fasterxml.jackson.databind.node.MissingNode",
            assertFailsWith<IllegalStateException> { TextFormat.parse("") }.message
        )
        assertEquals(
            "Parsed content is 'null'",
            assertFailsWith<IllegalStateException> { TextFormat.parse("null") }.message
        )
    }
}
