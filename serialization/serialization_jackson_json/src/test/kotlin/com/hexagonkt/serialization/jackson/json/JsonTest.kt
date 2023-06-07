package com.hexagonkt.serialization.jackson.json

import com.hexagonkt.serialization.SerializationFormat
import com.hexagonkt.serialization.parse
import com.hexagonkt.serialization.test.SerializationTest
import kotlin.test.Test
import java.io.ByteArrayOutputStream
import java.net.URL
import kotlin.test.assertEquals

internal class JsonTest : SerializationTest() {

    override val format: SerializationFormat = Json
    override val urls: List<URL> = listOf(
        URL("classpath:data/companies.json"),
        URL("classpath:data/company.json"),
    )

    @Test fun `Parse URL generates the correct collection`() {
        assert(URL("classpath:data/companies.json").parse() is List<*>)
        assert(URL("classpath:data/company.json").parse() is Map<*, *>)
    }

    @Test fun `Test Jackson text format`() {
        assert(Json.textFormat)
        val output = ByteArrayOutputStream()
        Json.serialize(mapOf("key" to "value"), output)
        val result = output.toString().trim().replace("\r", "").replace("\n", "")
        assertEquals("{  \"key\" : \"value\"}", result)
    }

    @Test fun `Test Jackson raw format (no pretty print)`() {
        assert(Json.raw.textFormat)
        val output = ByteArrayOutputStream()
        Json.raw.serialize(mapOf("key" to "value"), output)
        val result = output.toString().trim()
        assertEquals("{\"key\":\"value\"}", result)
    }
}
