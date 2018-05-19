package com.hexagonkt

import com.hexagonkt.vertx.serialization.JsonFormat
import com.hexagonkt.vertx.serialization.SerializationManager.getMimeTypeForFilename
import com.hexagonkt.vertx.serialization.YamlFormat
import org.junit.Test
import kotlin.test.assertFailsWith
import java.time.LocalDateTime.of as dateTime

class HelpersTest {
    private val m = mapOf(
        "alpha" to "bravo",
        "tango" to 0,
        "nested" to mapOf(
            "zulu" to "charlie"
        ),
        0 to 1
    )

    @Test fun `MIME types throw exception on incontent type`() {
        assertFailsWith<IllegalStateException> { getMimeTypeForFilename("") }
        assertFailsWith<IllegalStateException> { getMimeTypeForFilename("a") }
        assertFailsWith<IllegalStateException> { getMimeTypeForFilename(".") }
        assertFailsWith<IllegalStateException> { getMimeTypeForFilename("a.") }
        assertFailsWith<IllegalStateException> { getMimeTypeForFilename(".not_found") }
    }

    @Test fun `MIME types return correct content type`() {
        assert(getMimeTypeForFilename("a.json") == JsonFormat.contentType)
        assert(getMimeTypeForFilename("a.yaml") == YamlFormat.contentType)
        assert(getMimeTypeForFilename("a.yml") == YamlFormat.contentType)
        assert(getMimeTypeForFilename("a.png") == "image/png")
        assert(getMimeTypeForFilename("a.rtf") == "application/rtf")

        assert(getMimeTypeForFilename(".json") == JsonFormat.contentType)
        assert(getMimeTypeForFilename(".yaml") == YamlFormat.contentType)
        assert(getMimeTypeForFilename(".yml") == YamlFormat.contentType)
        assert(getMimeTypeForFilename(".png") == "image/png")
        assert(getMimeTypeForFilename(".rtf") == "application/rtf")
    }

    @Test fun `Get nested keys inside a map returns the proper value`() {
        assert(m["nested", "zulu"] == "charlie")
        assert(m["nested", "zulu", "tango"] == null)
        assert(m["nested", "empty"] == null)
        assert(m["empty"] == null)
        assert(m["alpha"] == "bravo")
        assert(m[0] == 1)
    }

    @Test fun `Require a value defined by a list of keys return the correct value`() {
        assert(m.require<String>("nested", "zulu") == "charlie")
        assert(m.require<String>("alpha") == "bravo")
        assert(m.require<Int>(0) == 1)
    }

    @Test(expected = IllegalStateException::class)
    fun `Require not found keys fails`() {
        m.require<Any>("nested", "zulu", "tango")
    }

    @Test(expected = IllegalStateException::class)
    fun `Require not found key in map fails`() {
        m.require<Any>("nested", "empty")
    }

    @Test(expected = IllegalStateException::class)
    fun `Require key not found first level throws an error`() {
        m.require<Any>("empty")
    }

    @Test fun `Filtered maps do not contain empty elements`() {
        assert(
            mapOf(
                "a" to "b",
                "b" to null,
                "c" to 1,
                "d" to listOf(1, 2),
                "e" to listOf<String>(),
                "f" to mapOf(0 to 1),
                "g" to mapOf<String, Int>(),
                "h" to mapOf("a" to true, "b" to null).filterEmpty(),
                "i" to mapOf("a" to listOf<Int>()).filterEmpty()
            ).filterEmpty() ==
            mapOf(
                "a" to "b",
                "c" to 1,
                "d" to listOf(1, 2),
                "f" to mapOf(0 to 1),
                "h" to mapOf("a" to true)
            )
        )
    }

    @Test fun `Filtered lists do not contain empty elements`() {
        assert(
            listOf(
                "a",
                null,
                listOf(1, 2),
                listOf<String>(),
                mapOf(0 to 1),
                mapOf<String, Int>(),
                mapOf("a" to true, "b" to null).filterEmpty(),
                mapOf("a" to listOf<Int>()).filterEmpty()
            ).filterEmpty() ==
            listOf(
                "a",
                listOf(1, 2),
                mapOf(0 to 1),
                mapOf("a" to true)
            )
        )
    }

    @Test(expected = IllegalStateException::class)
    fun `'error' generates the correct exception`() {
        error
    }
}
