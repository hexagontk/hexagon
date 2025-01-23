package com.hexagontk.serialization.jackson.json

import com.hexagontk.core.urlOf
import com.hexagontk.serialization.SerializationFormat
import com.hexagontk.serialization.parse
import com.hexagontk.serialization.test.SerializationTest
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.net.URL
import kotlin.test.assertEquals

internal class Json5Test : SerializationTest() {

    override val format: SerializationFormat = Json5
    override val urls: List<URL> = listOf(
        urlOf("classpath:data/companies_j5.json"),
        urlOf("classpath:data/company_j5.json"),
    )

    @Test fun `Parse URL generates the correct collection`() {
        assert(urlOf("classpath:data/companies_j5.json").parse() is List<*>)
        assert(urlOf("classpath:data/company_j5.json").parse() is Map<*, *>)
    }

    @Test fun `Test Jackson text format`() {
        assert(Json5.textFormat)
        val output = ByteArrayOutputStream()
        Json5.serialize(mapOf("key" to "value"), output)
        val result = output.toString().trim().replace("\r", "").replace("\n", "")
        assertEquals("{  key : \"value\"}", result)
    }

    @Test fun `Test Jackson raw format (no pretty print)`() {
        assert(Json5.raw.textFormat)
        val output = ByteArrayOutputStream()
        Json5.raw.serialize(mapOf("key" to "value"), output)
        val result = output.toString().trim()
        assertEquals("{key:\"value\"}", result)
    }
}
