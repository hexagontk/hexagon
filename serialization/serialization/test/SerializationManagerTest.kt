package com.hexagontk.serialization

import com.hexagontk.core.media.*
import com.hexagontk.core.urlOf
import com.hexagontk.serialization.SerializationManager.formatOf
import com.hexagontk.serialization.SerializationManager.formatOfOrNull
import com.hexagontk.serialization.SerializationManager.formats
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class SerializationManagerTest {

    @BeforeEach @AfterEach fun resetSerializationFormats() {
        formats = setOf(TextTestFormat)
    }

    @Test fun `User can add and remove serialization formats`() {
        assertEquals(TextTestFormat, formatOf(APPLICATION_PHP))

        formats = emptySet()
        assert(formats.isEmpty())

        formats = setOf(TextTestFormat)
        assertEquals(setOf(TextTestFormat), formats)
        assertEquals(TextTestFormat, formatOf(mediaTypeOf("php")))
        assertEquals(TextTestFormat, formatOf(APPLICATION_PHP))

        formats = setOf(TextTestFormat, BinaryTestFormat)
        assertEquals(setOf(TextTestFormat, BinaryTestFormat), formats)
        assertEquals(TextTestFormat, formatOf(mediaTypeOf("php")))
        assertEquals(TextTestFormat, formatOf(APPLICATION_PHP))
        assertEquals(BinaryTestFormat, formatOf(mediaTypeOf("avro")))
        assertEquals(BinaryTestFormat, formatOf(APPLICATION_AVRO))
    }

    @Test fun `Searching a format not loaded return null or raises an exception`() {
        assertNull(formatOfOrNull(TEXT_PLAIN))
        val e = assertFailsWith<IllegalStateException> {
            formats = setOf(TextTestFormat)
            formatOf(APPLICATION_JSON)
        }

        val mt = APPLICATION_JSON.fullType
        val amt = "application/x-httpd-php"
        assertEquals("Cannot find serialization format for: $mt. Available: $amt", e.message)
    }

    @Test fun `Searching serialization format for content types, URLs, files and resources works`() {
        assertEquals(TextTestFormat, formatOf(APPLICATION_PHP))
        assertEquals(TextTestFormat, formatOf(mediaTypeOf("php")))
        assertEquals(TextTestFormat, formatOf(mediaTypeOf(urlOf("http://l/a.php"))))
        assertEquals(TextTestFormat, formatOf(mediaTypeOf(File("f.php"))))
    }
}
