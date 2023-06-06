package com.hexagonkt.rest

import com.hexagonkt.core.media.APPLICATION_JSON
import com.hexagonkt.core.media.APPLICATION_YAML
import com.hexagonkt.http.model.HttpResponse
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.HttpRequest
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.jackson.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class RestTest {

    @Test fun `Media type is calculated properly`() {
        SerializationManager.defaultFormat = null
        assertFailsWith<IllegalStateException> { HttpRequest().mediaType() }
        assertFailsWith<IllegalStateException> { HttpResponse().mediaType() }
        SerializationManager.defaultFormat = Json
        assertEquals(APPLICATION_JSON, HttpRequest().mediaType())
        assertEquals(APPLICATION_JSON, HttpResponse().mediaType())
        HttpRequest(contentType = ContentType(APPLICATION_YAML)).apply {
            assertEquals(APPLICATION_YAML, mediaType())
        }
        HttpResponse(contentType = ContentType(APPLICATION_YAML)).apply {
            assertEquals(APPLICATION_YAML, mediaType())
        }
    }

    @Test fun `Body is parsed to list`() {
        SerializationManager.defaultFormat = Json
        HttpRequest(body = """[ "a", "b", "c" ]""").apply {
            assertEquals(APPLICATION_JSON, mediaType())
            assertEquals(listOf("a", "b", "c"), bodyList())
        }
        HttpResponse(body = """[ "a", "b", "c" ]""").apply {
            assertEquals(APPLICATION_JSON, mediaType())
            assertEquals(listOf("a", "b", "c"), bodyList())
        }
    }

    @Test fun `Body is parsed to map`() {
        SerializationManager.defaultFormat = Json
        HttpRequest(body = """{ "a" : 0, "b" : 1, "c" : 2 }""").apply {
            assertEquals(APPLICATION_JSON, mediaType())
            assertEquals(mapOf("a" to 0, "b" to 1, "c" to 2), bodyMap())
        }
        HttpResponse(body = """{ "a" : 0, "b" : 1, "c" : 2 }""").apply {
            assertEquals(APPLICATION_JSON, mediaType())
            assertEquals(mapOf("a" to 0, "b" to 1, "c" to 2), bodyMap())
        }
    }
}
