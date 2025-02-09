package com.hexagontk.core.media

import com.hexagontk.core.assertEnabled
import com.hexagontk.core.media.MediaTypeGroup.ANY

/**
 * Media type (also known as MIME type).
 */
class MediaType(
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
            mediaTypesExtensions.values.toSet().associateBy { it.fullType }
        }

        operator fun get(extension: String): MediaType =
            mediaTypesExtensions[extension] ?: DEFAULT_MEDIA_TYPE

        operator fun invoke(fullType: String): MediaType =
            fullTypes[fullType] ?: parseMediaType(fullType)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaType

        if (group != other.group) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = group.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}
