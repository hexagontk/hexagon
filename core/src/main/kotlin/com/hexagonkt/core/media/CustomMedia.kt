package com.hexagonkt.core.media

import com.hexagonkt.core.assertEnabled
import com.hexagonkt.core.media.MediaTypeGroup.ANY

/**
 * Create a media type (for types not included by default).
 */
data class CustomMedia(
    override val group: MediaTypeGroup,
    override val type: String,
) : MediaType {

    override val fullType: String = if (group == ANY) "*/$type" else "${group.text}/$type"

    init {
        if (assertEnabled)
            require(type.matches(mediaTypeFormat)) { "Type must match '$mediaTypeFormat': $type" }
    }
}
