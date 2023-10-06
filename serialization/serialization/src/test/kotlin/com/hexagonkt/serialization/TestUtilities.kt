package com.hexagonkt.serialization

import com.hexagonkt.core.*
import com.hexagonkt.core.media.APPLICATION_AVRO
import com.hexagonkt.core.media.APPLICATION_PHP
import java.io.InputStream
import java.io.OutputStream

internal data class Person(val name: String) : Data<Person> {

    override val data: Map<String, *> =
        fieldsMapOf(Person::name to name)

    override fun copy(data: Map<String, *>): Person =
        copy(name = data.getOrDefault(Person::name, name))
}

object TextTestFormat : SerializationFormat {
    override val mediaType = APPLICATION_PHP
    override val textFormat = true

    override fun serialize(instance: Any, output: OutputStream) {
        output.write(instance.toString().toByteArray())
    }

    override fun parse(input: InputStream): Any =
        listOf("text")
}

object BinaryTestFormat : SerializationFormat {
    override val mediaType = APPLICATION_AVRO
    override val textFormat = false

    override fun serialize(instance: Any, output: OutputStream) {
        output.write(instance.toString().toByteArray())
    }

    override fun parse(input: InputStream): Any =
        listOf("bytes")
}
