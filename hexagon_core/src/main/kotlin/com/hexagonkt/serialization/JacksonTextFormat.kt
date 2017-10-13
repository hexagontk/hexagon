package com.hexagonkt.serialization

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.util.DefaultIndenter.SYSTEM_LINEFEED_INSTANCE
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.*
import com.hexagonkt.serialization.JacksonSerializer.createObjectMapper

import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.KClass

internal open class JacksonTextFormat(
    extension: String, factoryGenerator: (() -> JsonFactory)? = null) : SerializationFormat {

    override val contentType = "application/$extension"
    override val isBinary = false

    private val mapper =
        if (factoryGenerator == null) JacksonSerializer.mapper
        else createObjectMapper(factoryGenerator())

    private val writer = createObjectWriter()

    private fun createObjectWriter (): ObjectWriter {
        val printer = DefaultPrettyPrinter ().withArrayIndenter (SYSTEM_LINEFEED_INSTANCE)
        return mapper.writer (printer)
    }

    override fun serialize(obj: Any, output: OutputStream) = writer.writeValue(output, obj)
    override fun serialize(obj: Any): String = writer.writeValueAsString (obj)

    override fun <T : Any> parse(input: InputStream, type: KClass<T>): T =
        mapper.readValue (input, type.java)

    override fun <T : Any> parseList(input: InputStream, type: KClass<T>): List<T> =
        mapper.readValue (input, collectionType(List::class, type))

    private fun <T : Collection<*>> collectionType(coll: KClass<T>, type: KClass<*>) =
        mapper.typeFactory.constructCollectionType(coll.java, type.java)
}
