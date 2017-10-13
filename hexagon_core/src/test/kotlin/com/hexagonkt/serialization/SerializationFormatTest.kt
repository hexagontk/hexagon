package com.hexagonkt.serialization

import org.testng.annotations.Test
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.KClass

@Test class SerializationFormatTest {
    class TextTestFormat : SerializationFormat {
        override val contentType = "text/test"
        override val isBinary = false

        override fun serialize(obj: Any, output: OutputStream) {
            output.write(obj.toString().toByteArray())
        }

        override fun <T : Any> parse(input: InputStream, type: KClass<T>): T { TODO("not used") }
        override fun <T : Any> parseList(input: InputStream, type: KClass<T>): List<T> {
            TODO("not used")
        }
    }

    class BinaryTestFormat : SerializationFormat {
        override val contentType = "binary/test"
        override val isBinary = true

        override fun serialize(obj: Any, output: OutputStream) { TODO("not used") }
        override fun <T : Any> parse(input: InputStream, type: KClass<T>): T { TODO("not used") }
        override fun <T : Any> parseList(input: InputStream, type: KClass<T>): List<T> {
            TODO("not used")
        }
    }

    @Test(expectedExceptions = arrayOf(IllegalStateException::class))
    fun `a binary format fails to serialize to a string` () {
        BinaryTestFormat().serialize("foo")
    }

    fun `a text format can be serialized to a string` () {
        assert (TextTestFormat().serialize("foo") == "foo")
    }

    fun `test Jackson test format` () {
        assert(!YamlFormat.isBinary)
        val output = ByteArrayOutputStream()
        YamlFormat.serialize(mapOf("key" to "value"), output)
        val result = output.toString().trim()
        assert(result == """key: "value"""")
    }
}
