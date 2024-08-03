package com.hexagonkt.rest

import com.hexagonkt.core.media.APPLICATION_JSON
import com.hexagonkt.core.media.APPLICATION_YAML
import com.hexagonkt.http.handlers.HttpContext
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.jackson.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

internal class SerializeRequestCallbackTest {

    private val callback by lazy { SerializeRequestCallback() }

    @Test fun `Serialization callback fails if no formats are defined`() {
        val formats = SerializationManager.formats

        SerializationManager.formats = emptySet()
        val e = assertFailsWith<IllegalStateException> { SerializeRequestCallback() }
        assertEquals("Serialization callbacks require at least one registered format", e.message)

        SerializationManager.formats = formats
    }

    @Test fun `Serialize empty request callback creates the proper response`() {
        SerializationManager.formats = setOf(Json)
        val stringContext = HttpContext().send(body = "")
        assertSame(stringContext, callback(stringContext))

        val binaryContext = HttpContext().send(body = ByteArray(0))
        assertSame(binaryContext, callback(binaryContext))
    }

    @Test fun `Serialize request callback creates the proper response`() {
        SerializationManager.formats = setOf(Json)
        val body = mapOf("key" to "value")

        val yaml = ContentType(APPLICATION_YAML)
        val yamlContext = HttpContext().receive(body = body, contentType = yaml)
        assertSame(yamlContext, callback(yamlContext))

        val json = ContentType(APPLICATION_JSON)
        val jsonContext = HttpContext().receive(body = body, contentType = json)
        val jsonBody = "{\n  \"key\" : \"value\"\n}".replace("\n", System.lineSeparator())
        assertEquals(jsonContext.receive(jsonBody), callback(jsonContext))
    }
}
