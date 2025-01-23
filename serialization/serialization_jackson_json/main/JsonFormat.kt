package com.hexagontk.serialization.jackson.json

import com.hexagontk.core.media.APPLICATION_JSON
import com.hexagontk.core.media.MediaType
import com.hexagontk.serialization.jackson.JacksonTextFormat

open class JsonFormat(prettyPrint: Boolean = true, relaxed: Boolean = false) :
    JacksonTextFormat(prettyPrint = prettyPrint, relaxed = relaxed) {

    override val mediaType: MediaType = APPLICATION_JSON
}
