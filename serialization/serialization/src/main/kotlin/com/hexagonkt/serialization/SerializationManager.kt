package com.hexagontk.serialization

import com.hexagontk.core.media.MediaType

object SerializationManager {

    private var formatsMap: Map<MediaType, SerializationFormat> = emptyMap()

    var formats: Set<SerializationFormat> = emptySet()
        set(value) {
            formatsMap = value.associateBy { it.mediaType }
            field = value
        }

    fun formatOfOrNull(mediaType: MediaType): SerializationFormat? =
        formatsMap[mediaType]

    fun formatOf(mediaType: MediaType): SerializationFormat =
        formatOfOrNull(mediaType) ?: error(formatNotFound(mediaType.fullType))

    private fun formatNotFound(fullType: String): String {
        val formatList = formats.joinToString(", ") { it.mediaType.fullType }
        return "Cannot find serialization format for: $fullType. Available: $formatList"
    }
}
