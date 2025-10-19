package com.hexagontk.serialization.jackson.yaml

import com.hexagontk.core.media.APPLICATION_YAML
import com.hexagontk.core.media.MediaType
import com.hexagontk.serialization.jackson.JacksonTextFormat
import tools.jackson.databind.ObjectMapper
import tools.jackson.dataformat.yaml.YAMLMapper
import tools.jackson.databind.DeserializationFeature.*
import tools.jackson.databind.MapperFeature.*
import tools.jackson.databind.SerializationFeature.*
import tools.jackson.dataformat.yaml.YAMLWriteFeature.*

open class YamlFormat(
    val prettyPrint: Boolean = true
) : JacksonTextFormat(prettyPrint) {

    override val mediaType: MediaType = APPLICATION_YAML

    override fun createRelaxedMapper(): ObjectMapper =
        createMapper()

    override fun createMapper(): ObjectMapper =
        baseMapper()
            .configure(WRITE_DOC_START_MARKER, false)
            .configure(ALWAYS_QUOTE_NUMBERS_AS_STRINGS, true)
            .configure(MINIMIZE_QUOTES, prettyPrint)
            .configure(INDENT_ARRAYS_WITH_INDICATOR, prettyPrint)
            .build()

    private fun baseMapper(): YAMLMapper.Builder =
        YAMLMapper.builder()
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(FAIL_ON_EMPTY_BEANS, false)
            .configure(FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
            .configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(SORT_PROPERTIES_ALPHABETICALLY, false)
}
