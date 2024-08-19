package com.hexagonkt.rest

import com.hexagonkt.core.media.APPLICATION_JSON
import com.hexagonkt.core.media.APPLICATION_TOML
import com.hexagonkt.core.media.APPLICATION_XML
import com.hexagonkt.core.media.APPLICATION_YAML
import com.hexagonkt.core.media.TEXT_CSV
import com.hexagonkt.core.requireInt
import com.hexagonkt.http.model.HttpResponse
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.HttpRequest
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.jackson.csv.Csv
import com.hexagonkt.serialization.jackson.json.Json
import com.hexagonkt.serialization.jackson.toml.Toml
import com.hexagonkt.serialization.jackson.xml.Xml
import com.hexagonkt.serialization.jackson.yaml.Yaml
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

    @BeforeAll fun setUp() {
        SerializationManager.formats = setOf(Json, Yaml, Xml, Toml, Csv)
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
        HttpRequest(body = """[ "a", "b", "c" ]""", contentType = jsonContentType).apply {
            assertEquals(APPLICATION_JSON, mediaType())
            assertEquals(listOf("a", "b", "c"), bodyList())
        }
        HttpResponse(body = """[ "a", "b", "c" ]""", contentType = jsonContentType).apply {
            assertEquals(APPLICATION_JSON, mediaType())
            assertEquals(listOf("a", "b", "c"), bodyList())
        }
    }

    @Test fun `Body is parsed to map`() {
        HttpRequest(body = """{"a":0,"b":1,"c":2}""", contentType = jsonContentType).apply {
            assertEquals(APPLICATION_JSON, mediaType())
            assertEquals(mapOf("a" to 0, "b" to 1, "c" to 2), bodyMap())
        }
        HttpResponse(body = """{"a":0,"b":1,"c":2}""", contentType = jsonContentType).apply {
            assertEquals(APPLICATION_JSON, mediaType())
            assertEquals(mapOf("a" to 0, "b" to 1, "c" to 2), bodyMap())
        }
    }

    @Test fun `Body is parsed to objects`() {
        HttpRequest(body = """{"a":0,"b":1,"c":2}""", contentType = jsonContentType).apply {
            assertEquals(APPLICATION_JSON, mediaType())
            assertEquals(Record(mapOf("a" to 0, "b" to 1, "c" to 2)), bodyObject(::Record))
        }
        HttpResponse(body = """{"a":0,"b":1,"c":2}""", contentType = jsonContentType).apply {
            assertEquals(APPLICATION_JSON, mediaType())
            assertEquals(Record(mapOf("a" to 0, "b" to 1, "c" to 2)), bodyObject(::Record))
        }
        HttpRequest(body = """[{"a":0,"b":1,"c":2}]""", contentType = jsonContentType).apply {
            assertEquals(APPLICATION_JSON, mediaType())
            assertEquals(listOf(Record(0, 1, 2)), bodyObjects(::Record))
        }
        HttpResponse(body = """[{"a":0,"b":1,"c":2}]""", contentType = jsonContentType).apply {
            assertEquals(APPLICATION_JSON, mediaType())
            assertEquals(listOf(Record(0, 1, 2)), bodyObjects(::Record))
        }
    }

    @Test fun `Body is serialized`() {
        HttpResponse(body = Record(0, 1, 2), contentType = jsonContentType).apply {
            assertEquals(APPLICATION_JSON, mediaType())
            val serializedBody = serializeBody().toString().replace(Regex("(\\s|\\n)*"), "")
            assertEquals("""{"a":0,"b":1,"c":2}""", serializedBody)
        }
        HttpResponse(body = Record(3, 4, 5), contentType = yamlContentType).apply {
            assertEquals(APPLICATION_YAML, mediaType())
            val serializedBody = serializeBody().toString().replace(Regex("(\\s|\\n)*"), "")
            assertEquals("""a:3b:4c:5""", serializedBody)
        }
        HttpResponse(body = Record(6, 7, 8), contentType = xmlContentType).apply {
            assertEquals(APPLICATION_XML, mediaType())
            val serializedBody = serializeBody().toString().replace(Regex("(\\s|\\n)*"), "")
            assertEquals("""<Record><a>6</a><b>7</b><c>8</c></Record>""", serializedBody)
        }
        HttpResponse(body = Record(9, 10, 11), contentType = tomlContentType).apply {
            assertEquals(APPLICATION_TOML, mediaType())
            val serializedBody = serializeBody().toString().replace(Regex("(\\s|\\n)*"), "")
            assertEquals("""a=9b=10c=11""", serializedBody)
        }
        HttpResponse(body = listOf(12, 13, 14), contentType = csvContentType).apply {
            assertEquals(TEXT_CSV, mediaType())
            val serializedBody = serializeBody().toString().replace(Regex("(\\s|\\n)*"), "")
            assertEquals("""12,13,14""", serializedBody)
        }
    }
}
