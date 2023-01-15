package com.hexagonkt.serialization

import com.hexagonkt.core.media.ApplicationMedia
import com.hexagonkt.core.media.TextMedia
import com.hexagonkt.core.media.mediaTypeOf
import com.hexagonkt.serialization.SerializationManager.defaultFormat
import com.hexagonkt.serialization.SerializationManager.formatOf
import com.hexagonkt.serialization.SerializationManager.formatOfOrNull
import com.hexagonkt.serialization.SerializationManager.formats
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import java.io.File
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class SerializationManagerTest {

    @BeforeEach @AfterEach fun resetSerializationFormats() {
        formats = setOf(TextTestFormat)
        defaultFormat = null
    }

    @Test fun `Default serialization format is handled properly`() {
        assertEquals(setOf(TextTestFormat), formats)
        assertNull(defaultFormat)

        defaultFormat = TextTestFormat
        assertEquals(setOf(TextTestFormat), formats)
        assertEquals(TextTestFormat, defaultFormat)

        defaultFormat = null
        assertEquals(setOf(TextTestFormat), formats)
        assertNull(defaultFormat)

        defaultFormat = BinaryTestFormat
        assertEquals(setOf(TextTestFormat, BinaryTestFormat), formats)
        assertEquals(BinaryTestFormat, defaultFormat)

        defaultFormat = null
        assertEquals(setOf(TextTestFormat, BinaryTestFormat), formats)
        assertNull(defaultFormat)

        formats = emptySet()
        assertEquals(emptySet(), formats)
        assertNull(defaultFormat)

        defaultFormat = BinaryTestFormat
        assertEquals(setOf(BinaryTestFormat), formats)
        assertEquals(BinaryTestFormat, defaultFormat)

        defaultFormat = TextTestFormat
        assertEquals(setOf(TextTestFormat, BinaryTestFormat), formats)
        assertEquals(TextTestFormat, defaultFormat)
    }

    @Test fun `User can add and remove serialization formats`() {
        assertEquals(TextTestFormat, formatOf(ApplicationMedia.PHP))

        formats = emptySet()
        assert(formats.isEmpty())

        formats = setOf(TextTestFormat)
        assertEquals(setOf(TextTestFormat), formats)
        assertEquals(TextTestFormat, formatOf(mediaTypeOf("php")))
        assertEquals(TextTestFormat, formatOf(ApplicationMedia.PHP))

        formats = setOf(TextTestFormat, BinaryTestFormat)
        assertEquals(setOf(TextTestFormat, BinaryTestFormat), formats)
        assertEquals(TextTestFormat, formatOf(mediaTypeOf("php")))
        assertEquals(TextTestFormat, formatOf(ApplicationMedia.PHP))
        assertEquals(BinaryTestFormat, formatOf(mediaTypeOf("avro")))
        assertEquals(BinaryTestFormat, formatOf(ApplicationMedia.AVRO))
    }

    @Test fun `Searching a format not loaded return null or raises an exception`() {
        assertNull(formatOfOrNull(TextMedia.PLAIN))
        val e = assertFailsWith<IllegalStateException> {
            formats = setOf(TextTestFormat)
            formatOf(ApplicationMedia.JSON)
        }

        val mt = ApplicationMedia.JSON.fullType
        val amt = "application/x-httpd-php"
        assertEquals("Cannot find serialization format for: $mt. Available: $amt", e.message)
    }

    @Test fun `Searching serialization format for content types, URLs, files and resources works`() {
        assertEquals(TextTestFormat, formatOf(ApplicationMedia.PHP))
        assertEquals(TextTestFormat, formatOf(mediaTypeOf("php")))
        assertEquals(TextTestFormat, formatOf(mediaTypeOf(URL("http://l/a.php"))))
        assertEquals(TextTestFormat, formatOf(mediaTypeOf(File("f.php"))))
    }
}
