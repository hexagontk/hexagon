package com.hexagontk.rest

import com.hexagontk.core.media.APPLICATION_JSON
import com.hexagontk.core.media.APPLICATION_YAML
import com.hexagontk.http.handlers.HttpContext
import com.hexagontk.http.model.ContentType
import com.hexagontk.serialization.SerializationManager
import com.hexagontk.serialization.jackson.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

internal class SerializeResponseCallbackTest {

    private val callback by lazy { SerializeResponseCallback() }

    @Test fun `Serialization callback fails if no formats are defined`() {
        val formats = SerializationManager.formats

        SerializationManager.formats = emptySet()
        val e = assertFailsWith<IllegalStateException> { SerializeResponseCallback() }
        assertEquals("Serialization callbacks require at least one registered format", e.message)

        SerializationManager.formats = formats
    }

    @Test fun `Serialize empty response callback creates the proper response`() {
        SerializationManager.formats = setOf(Json)
        val stringContext = HttpContext().send(body = "")
        assertSame(stringContext, callback(stringContext))

        val binaryContext = HttpContext().send(body = ByteArray(0))
        assertSame(binaryContext, callback(binaryContext))
    }

    @Test fun `Serialize response callback creates the proper response`() {
        SerializationManager.formats = setOf(Json)
        val body = mapOf("key" to "value")
        val jsonBody = "{\n  \"key\" : \"value\"\n}".replace("\n", System.lineSeparator())

        val yaml = ContentType(APPLICATION_YAML)
        val yamlContext = HttpContext().send(body = body, contentType = yaml)
        assertSame(yamlContext, callback(yamlContext))

        val anyMedia = ContentType(APPLICATION_YAML)
        val anyMediaContext = HttpContext().send(body = body, contentType = anyMedia)
        assertSame(anyMediaContext, callback(anyMediaContext))

        val json = ContentType(APPLICATION_JSON)
        val jsonContext = HttpContext().send(body = body, contentType = json)
        assertEquals(jsonContext.send(body = jsonBody).request, callback(jsonContext).request)
        assertEquals(jsonContext.send(body = jsonBody).response, callback(jsonContext).response)

        val acceptContext = HttpContext().send(body = body).receive(accept = listOf(json))
        val expectedContext = acceptContext.send(body = jsonBody, contentType = json)
        assertEquals(expectedContext.request, callback(acceptContext).request)
        assertEquals(expectedContext.response, callback(acceptContext).response)
    }
}
