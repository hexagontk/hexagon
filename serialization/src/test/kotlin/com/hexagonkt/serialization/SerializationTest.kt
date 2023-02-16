package com.hexagonkt.serialization

import com.hexagonkt.core.media.APPLICATION_PHP
import com.hexagonkt.core.media.APPLICATION_AVRO
import com.hexagonkt.serialization.jackson.json.Json
import kotlin.test.Test
import java.io.File
import kotlin.IllegalStateException
import java.net.URL
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class SerializationTest {

    @Test fun `Data serialization to file work properly`() {
        SerializationManager.defaultFormat = Json

        val map = mapOf("a" to "b")
        val file = File("build/a.json")

        map.serialize(file)
        assertEquals(map, file.parse())
    }

    @Test fun `Data serialization helpers work properly`() {
        SerializationManager.formats = setOf(BinaryTestFormat)
        SerializationManager.defaultFormat = TextTestFormat

        assertContentEquals("text".toByteArray(), "text".serializeBytes(APPLICATION_PHP))
        assertContentEquals("text".serializeBytes(APPLICATION_PHP), "text".serializeBytes())
        assertContentEquals("text".toByteArray(), "text".serializeBytes(APPLICATION_AVRO))
        assertEquals("text", "text".serialize(APPLICATION_PHP))
        assertEquals("text".serialize(APPLICATION_PHP), "text".serialize())
        assertFailsWith<IllegalStateException> { "text".serialize(APPLICATION_AVRO) }

        assertEquals(listOf("text"), "string".parse(APPLICATION_PHP))
        assertEquals("string".parse(APPLICATION_PHP), "string".parse())
        assertEquals("string".parse(APPLICATION_PHP), "string".parse(TextTestFormat))
        assertEquals(listOf("bytes"), "string".parse(APPLICATION_AVRO))

        assertEquals("string".parse(TextTestFormat), "string".parse(APPLICATION_PHP))
        assertEquals("string".parse(BinaryTestFormat), "string".parse(APPLICATION_AVRO))

        assertEquals(listOf("text"), URL("classpath:data/company.php").parse())
        assertEquals(listOf("bytes"), URL("classpath:data/company.avro").parse())

        val resources = "src/test/resources"
        val phpFile = File("$resources/data/company.php").let {
            if (it.exists()) it
            else File("serialization/$resources/data/company.php")
        }
        val avroFile = File("$resources/data/company.avro").let {
            if (it.exists()) it
            else File("serialization/$resources/data/company.avro")
        }
        assertEquals(listOf("text"), phpFile.parse())
        assertEquals(listOf("bytes"), avroFile.parse())
    }
}
