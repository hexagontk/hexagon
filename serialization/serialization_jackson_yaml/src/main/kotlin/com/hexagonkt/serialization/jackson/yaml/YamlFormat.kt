package com.hexagontk.serialization.jackson.yaml

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.*
import com.hexagontk.core.media.APPLICATION_YAML
import com.hexagontk.core.media.MediaType
import com.hexagontk.serialization.jackson.JacksonTextFormat

open class YamlFormat(
    prettyPrint: Boolean = true
) : JacksonTextFormat({ createYamlFactory(prettyPrint) }, prettyPrint) {

    private companion object {
        fun createYamlFactory(prettyPrint: Boolean): JsonFactory =
            with(YAMLFactory()) {
                configure(WRITE_DOC_START_MARKER, false)
                configure(ALWAYS_QUOTE_NUMBERS_AS_STRINGS, true)
                configure(MINIMIZE_QUOTES, prettyPrint)
                configure(INDENT_ARRAYS_WITH_INDICATOR, prettyPrint)
            }
    }

    override val mediaType: MediaType = APPLICATION_YAML
}
