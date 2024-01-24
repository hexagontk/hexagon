package com.hexagonkt.rest

import com.hexagonkt.core.media.APPLICATION_JSON
import com.hexagonkt.core.media.APPLICATION_YAML
import com.hexagonkt.http.handlers.HttpContext
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.jackson.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class SerializeResponseCallbackTest {

    private val callback = SerializeResponseCallback()

    @Test fun `Serialize empty response callback creates the proper response`() {
        val stringContext = HttpContext().send(body = "")
        assertSame(stringContext, callback(stringContext))

        val binaryContext = HttpContext().send(body = ByteArray(0))
        assertSame(binaryContext, callback(binaryContext))
    }

    @Test fun `Serialize response callback creates the proper response`() {
        SerializationManager.formats = setOf(Json)
        val body = mapOf("key" to "value")
        val jsonBody = "{\n  \"key\" : \"value\"\n}"

        val yaml = ContentType(APPLICATION_YAML)
        val yamlContext = HttpContext().send(body = body, contentType = yaml)
        assertSame(yamlContext, callback(yamlContext))

        val anyMedia = ContentType(APPLICATION_YAML)
        val anyMediaContext = HttpContext().send(body = body, contentType = anyMedia)
        assertSame(anyMediaContext, callback(anyMediaContext))

        val json = ContentType(APPLICATION_JSON)
        val jsonContext = HttpContext().send(body = body, contentType = json)
        assertEquals(jsonContext.send(body = jsonBody), callback(jsonContext))

        val acceptContext = HttpContext().send(body = body).receive(accept = listOf(json))
        val expectedContext = acceptContext.send(body = jsonBody, contentType = json)
        assertEquals(expectedContext, callback(acceptContext))
    }
}
