package com.hexagontk.serialization.jackson.csv

import tools.jackson.dataformat.csv.CsvMapper
import com.hexagontk.core.media.MediaType
import com.hexagontk.core.media.TEXT_CSV
import com.hexagontk.serialization.SerializationFormat
import tools.jackson.dataformat.csv.CsvReadFeature.*
import tools.jackson.dataformat.csv.CsvWriteFeature.*
import java.io.InputStream
import java.io.OutputStream

// TODO Allow configuring adapter passing settings line fields line
object Csv : SerializationFormat {

    override val mediaType: MediaType = TEXT_CSV
    override val textFormat: Boolean = true

    private val mapper: CsvMapper =
        CsvMapper.builder()
            .configure(ALWAYS_QUOTE_EMPTY_STRINGS, false)
            .configure(ALLOW_TRAILING_COMMA, false)
            .configure(SKIP_EMPTY_LINES, true)
            .configure(WRAP_AS_ARRAY, true)
            .build()

    private val reader = mapper.readerForListOf(Any::class.java)

    override fun serialize(instance: Any, output: OutputStream) {
        mapper.writeValue(output, instance)
    }

    override fun parse(input: InputStream): Any =
        reader.readValue(input)
}
