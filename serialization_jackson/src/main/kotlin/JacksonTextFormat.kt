package com.hexagonkt.serialization.jackson

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.util.DefaultIndenter.SYSTEM_LINEFEED_INSTANCE
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.hexagonkt.serialization.SerializationFormat
import com.hexagonkt.serialization.jackson.JacksonHelper.createObjectMapper
import com.hexagonkt.serialization.jackson.JacksonHelper.mapNode
import java.io.InputStream
import java.io.OutputStream

abstract class JacksonTextFormat(
    factoryGenerator: () -> JsonFactory = { JsonFactory() }
) : SerializationFormat {

    private val mapper by lazy {
        createObjectMapper(factoryGenerator())
    }

    override val textFormat = true

    private val writer by lazy {
        val printer = DefaultPrettyPrinter().withArrayIndenter(SYSTEM_LINEFEED_INSTANCE)
        mapper.writer(printer)
    }

    override fun serialize(instance: Any, output: OutputStream) =
        writer.writeValue(output, instance)

    override fun serialize(instance: Any): String =
        writer.writeValueAsString(instance)

    override fun parse(input: InputStream): Any =
        mapNode(mapper.readTree(input))
}
