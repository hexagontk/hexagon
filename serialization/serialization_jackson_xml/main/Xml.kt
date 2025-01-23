package com.hexagontk.serialization.jackson.xml

import com.fasterxml.jackson.databind.DeserializationFeature.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter
import com.hexagontk.core.media.APPLICATION_XML
import com.hexagontk.core.media.MediaType
import com.hexagontk.serialization.SerializationFormat
import com.hexagontk.serialization.jackson.JacksonHelper.mapNode
import java.io.InputStream
import java.io.OutputStream

// TODO Implement with Java XML support (Jackson is not the best option here)
object Xml : SerializationFormat {

    private val mapper: ObjectMapper = XmlMapper(JacksonXmlModule())
        .configure(INDENT_OUTPUT, true)
        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(FAIL_ON_EMPTY_BEANS, false)
        .configure(FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
        .configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true)

    override val mediaType: MediaType = APPLICATION_XML
    override val textFormat: Boolean = true

    private val writer = createObjectWriter()

    private fun createObjectWriter(): ObjectWriter =
        mapper.writer(DefaultXmlPrettyPrinter())

    override fun serialize(instance: Any, output: OutputStream) =
        writer.writeValue(output, instance)

    override fun serialize(instance: Any): String =
        writer.writeValueAsString(instance)

    override fun parse(input: InputStream): Any =
        mapNode(mapper.readTree(input))
}
