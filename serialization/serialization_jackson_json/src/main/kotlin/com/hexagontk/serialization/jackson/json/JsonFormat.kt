package com.hexagontk.serialization.jackson.json

import com.hexagontk.core.media.APPLICATION_JSON
import com.hexagontk.core.media.MediaType
import com.hexagontk.serialization.jackson.JacksonTextFormat

open class JsonFormat(prettyPrint: Boolean = true) : JacksonTextFormat(prettyPrint = prettyPrint) {
    override val mediaType: MediaType = APPLICATION_JSON
}
