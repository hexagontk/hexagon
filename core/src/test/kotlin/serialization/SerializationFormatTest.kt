package com.hexagonkt.core.serialization

import com.hexagonkt.serialization.yaml.Yaml
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.KClass
import kotlin.test.assertFailsWith

internal class SerializationFormatTest {

    class TextTestFormat : SerializationFormat {
        override val contentType = "text/test"
        override val extensions = setOf("test")
        override val isBinary = false

        override fun serialize(obj: Any, output: OutputStream) {
            output.write(obj.toString().toByteArray())
        }

        override fun <T : Any> parse(input: InputStream, type: KClass<T>): T { TODO("not used") }
        override fun <T : Any> parseObjects(input: InputStream, type: KClass<T>): List<T> {
            TODO("not used")
        }
    }

    class BinaryTestFormat : SerializationFormat {
        override val contentType = "binary/test"
        override val extensions = setOf("test")
        override val isBinary = true

        override fun serialize(obj: Any, output: OutputStream) { TODO("not used") }
        override fun <T : Any> parse(input: InputStream, type: KClass<T>): T { TODO("not used") }
        override fun <T : Any> parseObjects(input: InputStream, type: KClass<T>): List<T> {
            TODO("not used")
        }
    }

    @Test fun `A binary format fails to serialize to a string` () {
        assertFailsWith<IllegalStateException> {
            BinaryTestFormat().serialize("foo")
        }
    }

    @Test fun `A text format can be serialized to a string` () {
        assert (TextTestFormat().serialize("foo") == "foo")
    }

    @Test fun `Test Jackson text format` () {
        assert(!Yaml.isBinary)
        val output = ByteArrayOutputStream()
        Yaml.serialize(mapOf("key" to "value"), output)
        val result = output.toString().trim()
        assert(result == """key: "value"""")
    }
}
