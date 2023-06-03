package com.hexagonkt.serialization.jackson.toml

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.dataformat.toml.TomlFactory
import com.hexagonkt.core.media.APPLICATION_TOML
import com.hexagonkt.core.media.MediaType
import com.hexagonkt.serialization.jackson.JacksonTextFormat

object Toml : JacksonTextFormat({ Toml.createTomlFactory() }) {

    private fun createTomlFactory(): JsonFactory =
        TomlFactory()

    override val mediaType: MediaType = APPLICATION_TOML
}
