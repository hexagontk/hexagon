package com.hexagonkt.serialization.jackson.json

import com.hexagonkt.core.media.ApplicationMedia
import com.hexagonkt.core.media.MediaType
import com.hexagonkt.serialization.jackson.JacksonTextFormat

object Json : JacksonTextFormat() {
    override val mediaType: MediaType = ApplicationMedia.JSON
}
