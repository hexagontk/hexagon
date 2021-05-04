package com.hexagonkt.serialization

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.dataformat.csv.CsvGenerator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.hexagonkt.serialization.JacksonHelper.setupObjectMapper
import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.KClass

object Csv : SerializationFormat {

    override val contentType: String = "text/csv"
    override val extensions: Set<String> = setOf("csv")
    override val isBinary: Boolean = false

    private val mapper: CsvMapper = setupObjectMapper(
        CsvMapper()
            .configure(CsvGenerator.Feature.ALWAYS_QUOTE_EMPTY_STRINGS, false)
            .configure(CsvParser.Feature.ALLOW_TRAILING_COMMA, false)
            .configure(CsvParser.Feature.SKIP_EMPTY_LINES, true)
    ) as CsvMapper

    override fun serialize(obj: Any, output: OutputStream) {
        val schema = when (obj) {
            is Collection<*> -> mapper.schemaFor(obj.firstOrNull()?.javaClass ?: Any::class.java)
            else -> mapper.schemaFor(obj.javaClass)
        }

        mapper.writer(schema).writeValue(output, obj)
    }

    override fun <T : Any> parse(input: InputStream, type: KClass<T>): T =
        try {
            objectReader(type).readValue(input)
        }
        catch (e: JsonProcessingException) {
            val field: String = (e as? JsonMappingException)?.pathReference ?: ""
            throw ParseException(field, e)
        }

    override fun <T : Any> parseObjects(input: InputStream, type: KClass<T>): List<T> =
        try {
            objectReader(type).readValues<T>(input).readAll()
        }
        catch (e: JsonProcessingException) {
            val field: String = (e as? JsonMappingException)?.pathReference ?: ""
            throw ParseException(field, e)
        }

    private fun <T : Any> objectReader(type: KClass<T>): ObjectReader =
        mapper.readerFor(type.java).with(mapper.schemaFor(type.java))
}
