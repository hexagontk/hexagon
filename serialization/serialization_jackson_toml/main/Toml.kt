package com.hexagontk.serialization.jackson.toml

import com.hexagontk.core.media.APPLICATION_TOML
import com.hexagontk.core.media.MediaType
import com.hexagontk.serialization.jackson.JacksonTextFormat

import tools.jackson.databind.ObjectMapper
import tools.jackson.dataformat.toml.TomlMapper
import tools.jackson.databind.DeserializationFeature.*
import tools.jackson.databind.MapperFeature.*
import tools.jackson.databind.SerializationFeature.*

object Toml : JacksonTextFormat() {

    override val mediaType: MediaType = APPLICATION_TOML

    override fun createRelaxedMapper(): ObjectMapper =
        createMapper()

    override fun createMapper(): ObjectMapper =
        baseMapper().build()

    private fun baseMapper(): TomlMapper.Builder =
        TomlMapper.builder()
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(FAIL_ON_EMPTY_BEANS, false)
            .configure(FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
            .configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(SORT_PROPERTIES_ALPHABETICALLY, false)
}
