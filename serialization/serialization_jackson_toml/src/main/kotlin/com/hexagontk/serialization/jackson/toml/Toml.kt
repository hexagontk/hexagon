package com.hexagontk.serialization.jackson.toml

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.dataformat.toml.TomlFactory
import com.hexagontk.core.media.APPLICATION_TOML
import com.hexagontk.core.media.MediaType
import com.hexagontk.serialization.jackson.JacksonTextFormat

object Toml : JacksonTextFormat({ Toml.createTomlFactory() }) {

    private fun createTomlFactory(): JsonFactory =
        TomlFactory()

    override val mediaType: MediaType = APPLICATION_TOML
}
