package com.hexagontk.serialization.jackson

import com.hexagontk.core.media.APPLICATION_JSON
import com.hexagontk.core.media.MediaType
import com.hexagontk.serialization.serialize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class JacksonTextFormatTest {

    object RelaxedTextFormat : JacksonTextFormat(relaxed = true) {
        override val mediaType: MediaType = APPLICATION_JSON
    }

    object TextFormat : JacksonTextFormat() {
        override val mediaType: MediaType = APPLICATION_JSON
    }

    @Test fun `Parse objects to nodes`() {
        assertEquals(emptyList<Any>(), RelaxedTextFormat.parse("[]"))
        assertEquals(emptyMap<Any, Any>(), RelaxedTextFormat.parse("{}"))

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
                    text : "text",
                    "boolean" : true,
                    "double" : 0.123456789,
                },
                "long" : 1234567890123456789,
                null : null,
            }
        """

        assertEquals(expected, RelaxedTextFormat.parse(json))
        assertEquals(
            """
                {
                  array : [
                    1,
                    2,
                    3
                  ],
                  object : {
                    text : "text",
                    boolean : true,
                    double : 0.123456789
                  },
                  long : 1234567890123456789,
                  null : null
                }
            """.trimIndent(),
            RelaxedTextFormat.parse(json).serialize(RelaxedTextFormat)
        )
    }

    @Test fun `Errors parsing input are handled`() {
        assertEquals(
            "Unknown node type: tools.jackson.databind.node.MissingNode",
            assertFailsWith<IllegalStateException> { RelaxedTextFormat.parse("") }.message
        )
        assertEquals(
            "Parsed content is 'null'",
            assertFailsWith<IllegalStateException> { RelaxedTextFormat.parse("null") }.message
        )
    }

    @Test fun `Parse objects to nodes (strict)`() {
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
                    "double" : 0.123456789
                },
                "long" : 1234567890123456789,
                "null" : null
            }
        """

        assertEquals(expected, TextFormat.parse(json))
        assertEquals(
            """
                {
                  "array" : [
                    1,
                    2,
                    3
                  ],
                  "object" : {
                    "text" : "text",
                    "boolean" : true,
                    "double" : 0.123456789
                  },
                  "long" : 1234567890123456789,
                  "null" : null
                }
            """.trimIndent(),
            TextFormat.parse(json).serialize(TextFormat)
        )
    }

    @Test fun `Errors parsing input are handled (strict)`() {
        assertEquals(
            "Unknown node type: tools.jackson.databind.node.MissingNode",
            assertFailsWith<IllegalStateException> { TextFormat.parse("") }.message
        )
        assertEquals(
            "Parsed content is 'null'",
            assertFailsWith<IllegalStateException> { TextFormat.parse("null") }.message
        )
    }
}
