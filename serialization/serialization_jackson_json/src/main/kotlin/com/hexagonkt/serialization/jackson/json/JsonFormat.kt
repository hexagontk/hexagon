package com.hexagonkt.serialization.jackson.json

import com.hexagonkt.core.media.APPLICATION_JSON
import com.hexagonkt.core.media.MediaType
import com.hexagonkt.serialization.jackson.JacksonTextFormat

open class JsonFormat(prettyPrint: Boolean = true) : JacksonTextFormat(prettyPrint = prettyPrint) {
    override val mediaType: MediaType = APPLICATION_JSON
}
