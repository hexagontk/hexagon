package com.hexagonkt.core.media

import com.hexagonkt.core.assertEnabled
import com.hexagonkt.core.media.MediaTypeGroup.ANY

/**
 * Media type (also known as MIME type).
 */
data class MediaType(
    val group: MediaTypeGroup,
    val type: String,
) {
    val fullType: String = if (group == ANY) "*/$type" else "${group.text}/$type"

    init {
        if (assertEnabled)
            require(type.matches(MEDIA_TYPE_FORMAT)) {
                "Type must match '$MEDIA_TYPE_FORMAT': $type"
            }
    }

    companion object {
        val fullTypes: Map<String, MediaType> by lazy {
            MEDIA_TYPES_EXTENSIONS.values.toSet().associateBy { it.fullType }
        }

        operator fun get(extension: String): MediaType =
            MEDIA_TYPES_EXTENSIONS[extension] ?: DEFAULT_MEDIA_TYPE

        operator fun invoke(fullType: String): MediaType =
            fullTypes[fullType] ?: parseMediaType(fullType)
    }
}
