package com.hexagontk.serialization.jackson

import com.hexagontk.serialization.SerializationFormat
import com.hexagontk.serialization.jackson.JacksonHelper.mapNode

import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.core.json.JsonReadFeature.*
import tools.jackson.core.json.JsonWriteFeature.*
import tools.jackson.core.util.DefaultIndenter.SYSTEM_LINEFEED_INSTANCE
import tools.jackson.core.util.DefaultPrettyPrinter
import tools.jackson.databind.DeserializationFeature.*
import tools.jackson.databind.MapperFeature.*
import tools.jackson.databind.SerializationFeature.*

import java.io.InputStream
import java.io.OutputStream

abstract class JacksonTextFormat(
    prettyPrint: Boolean = true,
    relaxed: Boolean = false,
) : SerializationFormat {

    private val mapper by lazy {
        if (relaxed)
            createRelaxedMapper()
        else
            createMapper()
    }

    override val textFormat = true

    private val writer by lazy {
        if (prettyPrint)
            mapper.writerWithDefaultPrettyPrinter()
        else
            mapper.writer()
    }

    override fun serialize(instance: Any, output: OutputStream) =
        writer.writeValue(output, instance)

    override fun serialize(instance: Any): String =
        writer.writeValueAsString(instance)

    override fun parse(input: InputStream): Any =
        mapNode(mapper.readTree(input))

    protected open fun createRelaxedMapper(): ObjectMapper =
        baseMapper()
            .configure(ALLOW_UNQUOTED_PROPERTY_NAMES, true)
            .configure(ALLOW_JAVA_COMMENTS, true)
            .configure(ALLOW_SINGLE_QUOTES, true)
            .configure(ALLOW_TRAILING_COMMA, true)
            .configure(ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)
            .configure(ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS, true)
            .configure(ALLOW_UNESCAPED_CONTROL_CHARS, true)
            .configure(QUOTE_PROPERTY_NAMES, false)
            .build()

    protected open fun createMapper(): ObjectMapper =
        baseMapper().build()

    private fun baseMapper(): JsonMapper.Builder =
        JsonMapper.builder()
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(FAIL_ON_EMPTY_BEANS, false)
            .configure(FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
            .configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(SORT_PROPERTIES_ALPHABETICALLY, false)
            .defaultPrettyPrinter(
                DefaultPrettyPrinter().withArrayIndenter(SYSTEM_LINEFEED_INSTANCE)
            )
}
