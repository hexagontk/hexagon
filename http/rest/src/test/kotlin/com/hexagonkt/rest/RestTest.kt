package com.hexagontk.rest

import com.hexagontk.core.media.APPLICATION_JSON
import com.hexagontk.core.media.APPLICATION_TOML
import com.hexagontk.core.media.APPLICATION_XML
import com.hexagontk.core.media.APPLICATION_YAML
import com.hexagontk.core.media.TEXT_CSV
import com.hexagontk.core.requireInt
import com.hexagontk.http.model.HttpResponse
import com.hexagontk.http.model.ContentType
import com.hexagontk.http.model.HttpRequest
import com.hexagontk.serialization.SerializationManager
import com.hexagontk.serialization.jackson.csv.Csv
import com.hexagontk.serialization.jackson.json.Json
import com.hexagontk.serialization.jackson.toml.Toml
import com.hexagontk.serialization.jackson.xml.Xml
import com.hexagontk.serialization.jackson.yaml.Yaml
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
        val body1 = mapOf("a" to 0, "b" to 1, "c" to 2)
        HttpResponse(body = body1, contentType = jsonContentType).apply {
            assertEquals(APPLICATION_JSON, mediaType())
            val textBody = serializeBody().toString().replace(Regex("(\\s|\\n)*"), "")
            assertEquals("""{"a":0,"b":1,"c":2}""", textBody)
        }
        val body2 = mapOf("a" to 3, "b" to 4, "c" to 5)
        HttpResponse(body = body2, contentType = yamlContentType).apply {
            assertEquals(APPLICATION_YAML, mediaType())
            val textBody = serializeBody().toString().replace(Regex("(\\s|\\n)*"), "")
            assertEquals("""a:3b:4c:5""", textBody)
        }
        val body3 = mapOf("a" to 6, "b" to 7, "c" to 8)
        HttpResponse(body = body3, contentType = xmlContentType).apply {
            assertEquals(APPLICATION_XML, mediaType())
            val textBody = serializeBody().toString().replace(Regex("(\\s|\\n)*"), "")
            assertEquals("""<LinkedHashMap><a>6</a><b>7</b><c>8</c></LinkedHashMap>""", textBody)
        }
        val body4 = mapOf("a" to 9, "b" to 10, "c" to 11)
        HttpResponse(body = body4, contentType = tomlContentType).apply {
            assertEquals(APPLICATION_TOML, mediaType())
            val textBody = serializeBody().toString().replace(Regex("(\\s|\\n)*"), "")
            assertEquals("""a=9b=10c=11""", textBody)
        }
        val body5 = listOf(12, 13, 14)
        HttpResponse(body = body5, contentType = csvContentType).apply {
            assertEquals(TEXT_CSV, mediaType())
            val textBody = serializeBody().toString().replace(Regex("(\\s|\\n)*"), "")
            assertEquals("""12,13,14""", textBody)
        }
    }
}
