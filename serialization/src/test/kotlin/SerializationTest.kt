package com.hexagonkt.serialization

import com.hexagonkt.core.media.ApplicationMedia.PHP
import com.hexagonkt.core.media.ApplicationMedia.AVRO
import org.junit.jupiter.api.Test
import java.io.File
import java.lang.IllegalStateException
import java.net.URL
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class SerializationTest {

    @Test fun `Data serialization helpers work properly`() {
        SerializationManager.formats = setOf(BinaryTestFormat)
        SerializationManager.defaultFormat = TextTestFormat

        assertContentEquals("text".toByteArray(), "text".serializeBytes(PHP))
        assertContentEquals("text".serializeBytes(PHP), "text".serializeBytes())
        assertContentEquals("text".toByteArray(), "text".serializeBytes(AVRO))
        assertEquals("text", "text".serialize(PHP))
        assertEquals("text".serialize(PHP), "text".serialize())
        assertFailsWith<IllegalStateException> { "text".serialize(AVRO) }

        assertEquals(listOf("text"), "string".parse(PHP))
        assertEquals("string".parse(PHP), "string".parse())
        assertEquals("string".parse(PHP), "string".parse(TextTestFormat))
        assertEquals(listOf("bytes"), "string".parse(AVRO))

        assertEquals("string".parse(TextTestFormat), "string".parse(PHP))
        assertEquals("string".parse(BinaryTestFormat), "string".parse(AVRO))

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
