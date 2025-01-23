package com.hexagontk.serialization.dsl.json

import com.hexagontk.core.urlOf
import com.hexagontk.serialization.SerializationFormat
import com.hexagontk.serialization.parse
import com.hexagontk.serialization.test.SerializationTest
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import kotlin.IllegalStateException
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class JsonTest : SerializationTest() {

    override val format: SerializationFormat = Json
    override val urls: List<URL> = listOf(
        urlOf("classpath:data/companies.json"),
        urlOf("classpath:data/company.json"),
    )

    @Test fun `Parse URL generates the correct collection`() {
        assert(urlOf("classpath:data/companies.json").parse() is List<*>)
        assert(urlOf("classpath:data/company.json").parse() is Map<*, *>)
    }

    @Test fun `Test Jackson text format`() {
        assert(Json.textFormat)
        val output = ByteArrayOutputStream()
        Json.serialize(mapOf("key" to "value"), output)
        val result = output.toString().trim().replace("\r", "").replace("\n", "")
        assertEquals("{  \"key\": \"value\"}", result)
    }

    @Test fun `Test Jackson raw format (no pretty print)`() {
        assert(Json.raw.textFormat)
        val output = ByteArrayOutputStream()
        Json.raw.serialize(mapOf("key" to "value"), output)
        val result = output.toString().trim()
        assertEquals("{\"key\":\"value\"}", result)
    }

    @Test fun `Incorrect JSON throws an exception`() {
        assertFailsWith<IllegalStateException> { Json.parse("null") }
    }
}
