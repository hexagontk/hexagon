package com.hexagonkt.serialization.jackson.json

import com.hexagonkt.core.media.ApplicationMedia
import com.hexagonkt.core.media.MediaType
import com.hexagonkt.serialization.jackson.JacksonTextFormat

open class JsonFormat(prettyPrint: Boolean = true) : JacksonTextFormat(prettyPrint = prettyPrint) {
    override val mediaType: MediaType = ApplicationMedia.JSON
}
