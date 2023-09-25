package com.hexagonkt.rest

import com.hexagonkt.core.media.APPLICATION_JSON
import com.hexagonkt.core.media.APPLICATION_YAML
import com.hexagonkt.core.requireInt
import com.hexagonkt.http.model.HttpResponse
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.HttpRequest
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.jackson.json.Json
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@TestInstance(PER_CLASS)
internal class RestTest {

    private data class Record(
        val a: Int,
        val b: Int,
        val c: Int,
    ) {
        constructor(data: Map<String, *>) : this(
            data.requireInt(Record::a),
            data.requireInt(Record::b),
            data.requireInt(Record::c),
        )
    }

    private val json: ContentType = ContentType(APPLICATION_JSON)

    @BeforeAll fun setUp() {
        SerializationManager.formats = setOf(Json)
    }

    @Test fun `Media type is calculated properly`() {
        assertFailsWith<IllegalStateException> { HttpRequest().mediaType() }
        assertFailsWith<IllegalStateException> { HttpResponse().mediaType() }
        HttpRequest(contentType = ContentType(APPLICATION_YAML)).apply {
            assertEquals(APPLICATION_YAML, mediaType())
        }
        HttpResponse(contentType = ContentType(APPLICATION_YAML)).apply {
            assertEquals(APPLICATION_YAML, mediaType())
        }
    }

    @Test fun `Body is parsed to list`() {
        HttpRequest(body = """[ "a", "b", "c" ]""", contentType = json).apply {
            assertEquals(APPLICATION_JSON, mediaType())
            assertEquals(listOf("a", "b", "c"), bodyList())
        }
        HttpResponse(body = """[ "a", "b", "c" ]""", contentType = json).apply {
            assertEquals(APPLICATION_JSON, mediaType())
            assertEquals(listOf("a", "b", "c"), bodyList())
        }
    }

    @Test fun `Body is parsed to map`() {
        HttpRequest(body = """{ "a" : 0, "b" : 1, "c" : 2 }""", contentType = json).apply {
            assertEquals(APPLICATION_JSON, mediaType())
            assertEquals(mapOf("a" to 0, "b" to 1, "c" to 2), bodyMap())
        }
        HttpResponse(body = """{ "a" : 0, "b" : 1, "c" : 2 }""", contentType = json).apply {
            assertEquals(APPLICATION_JSON, mediaType())
            assertEquals(mapOf("a" to 0, "b" to 1, "c" to 2), bodyMap())
        }
    }

    @Test fun `Body is parsed to objects`() {
        HttpRequest(body = """{ "a" : 0, "b" : 1, "c" : 2 }""", contentType = json).apply {
            assertEquals(APPLICATION_JSON, mediaType())
            assertEquals(Record(mapOf("a" to 0, "b" to 1, "c" to 2)), bodyObject(::Record))
        }
        HttpResponse(body = """{ "a" : 0, "b" : 1, "c" : 2 }""", contentType = json).apply {
            assertEquals(APPLICATION_JSON, mediaType())
            assertEquals(Record(mapOf("a" to 0, "b" to 1, "c" to 2)), bodyObject(::Record))
        }
        HttpRequest(body = """[ { "a" : 0, "b" : 1, "c" : 2 } ]""", contentType = json).apply {
            assertEquals(APPLICATION_JSON, mediaType())
            assertEquals(listOf(Record(0, 1, 2)), bodyObjects(::Record))
        }
        HttpResponse(body = """[ { "a" : 0, "b" : 1, "c" : 2 } ]""", contentType = json).apply {
            assertEquals(APPLICATION_JSON, mediaType())
            assertEquals(listOf(Record(0, 1, 2)), bodyObjects(::Record))
        }
    }
}
