package com.hexagonkt.serialization

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter
import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.KClass

object Xml : SerializationFormat {

    private val mapper = JacksonHelper
        .setupObjectMapper(XmlMapper(JacksonXmlModule()))
        .configure(INDENT_OUTPUT, true)

    override val extensions: Set<String> = setOf("xml")
    override val contentType = "application/${extensions.first()}"
    override val isBinary = false

    private val writer = createObjectWriter()

    private fun createObjectWriter(): ObjectWriter =
        mapper.writer(DefaultXmlPrettyPrinter())

    override fun serialize(obj: Any, output: OutputStream) = writer.writeValue(output, obj)
    override fun serialize(obj: Any): String = writer.writeValueAsString(obj)

    override fun <T : Any> parse(input: InputStream, type: KClass<T>): T =
        try {
            mapper.readValue(input, type.java)
        }
        catch (e: JsonProcessingException) {
            throw ParseException(e)
        }

    override fun <T : Any> parseObjects(input: InputStream, type: KClass<T>): List<T> =
        try {
            mapper.readValue(input, collectionType(List::class, type))
        }
        catch (e: JsonProcessingException) {
            throw ParseException(e)
        }

    private fun <T : Collection<*>> collectionType(coll: KClass<T>, type: KClass<*>) =
        mapper.typeFactory.constructCollectionType(coll.java, type.java)
}
