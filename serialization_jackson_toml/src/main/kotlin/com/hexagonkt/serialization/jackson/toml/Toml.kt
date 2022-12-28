package com.hexagonkt.serialization.jackson.toml

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.dataformat.toml.TomlFactory
import com.fasterxml.jackson.dataformat.toml.TomlReadFeature.PARSE_JAVA_TIME
import com.hexagonkt.core.media.ApplicationMedia
import com.hexagonkt.core.media.MediaType
import com.hexagonkt.serialization.jackson.JacksonTextFormat

object Toml : JacksonTextFormat({ Toml.createTomlFactory() }) {

    private fun createTomlFactory(): JsonFactory =
        with(TomlFactory()) {
            configure(PARSE_JAVA_TIME, true)
        }

    override val mediaType: MediaType = ApplicationMedia.TOML
}
