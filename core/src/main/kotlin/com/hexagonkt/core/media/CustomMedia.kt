package com.hexagonkt.core.media

import com.hexagonkt.core.disableChecks

/**
 * Create a media type (for types not included by default).
 */
data class CustomMedia(
    override val group: MediaTypeGroup,
    override val type: String,
) : MediaType {

    override val fullType: String = "${group.text}/$type"

    init {
        if (!disableChecks) {
            require(type.matches(mediaTypeFormat)) {
                "Type must match '$mediaTypeFormat': $type"
            }
        }
    }
}
