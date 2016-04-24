package co.there4.hexagon.serialization

import org.testng.annotations.Test
import kotlin.test.assertFailsWith

@Test class JacksonSerializerTest {
    fun serializing_an_unsupported_content_type_fails () {
        assertFailsWith<IllegalArgumentException> {
            JacksonSerializer.serialize("text", "invalid/type")
        }
    }
}
