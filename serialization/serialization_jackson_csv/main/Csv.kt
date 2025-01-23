package com.hexagontk.serialization.jackson.csv

import com.fasterxml.jackson.dataformat.csv.CsvGenerator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.hexagontk.core.media.MediaType
import com.hexagontk.core.media.TEXT_CSV
import com.hexagontk.serialization.SerializationFormat
import java.io.InputStream
import java.io.OutputStream

// TODO Allow configuring adapter passing settings line fields line
object Csv : SerializationFormat {

    override val mediaType: MediaType = TEXT_CSV
    override val textFormat: Boolean = true

    private val mapper: CsvMapper =
        CsvMapper()
            .configure(CsvGenerator.Feature.ALWAYS_QUOTE_EMPTY_STRINGS, false)
            .configure(CsvParser.Feature.ALLOW_TRAILING_COMMA, false)
            .configure(CsvParser.Feature.SKIP_EMPTY_LINES, true)
            .configure(CsvParser.Feature.WRAP_AS_ARRAY, true)

    private val reader = mapper.readerForListOf(Any::class.java)

    override fun serialize(instance: Any, output: OutputStream) {
        mapper.writeValue(output, instance)
    }

    override fun parse(input: InputStream): Any =
        reader.readValue(input)
}
