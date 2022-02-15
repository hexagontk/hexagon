package com.hexagonkt.serialization

import com.hexagonkt.core.media.MediaType

object SerializationManager {

    private var formatsMap: Map<MediaType, SerializationFormat> = emptyMap()

    var formats: Set<SerializationFormat> = emptySet()
        set(value) {

            val allFormats =
                if (defaultFormat == null) value
                else (value + defaultFormat).filterNotNull()

            formatsMap = allFormats.associateBy { it.mediaType }

            field = value
        }

    var defaultFormat: SerializationFormat? = null
        set(value) {

            if (value != null)
                formats = formats + value

            field = value
        }

    fun requireDefaultFormat(): SerializationFormat =
        defaultFormat ?: error("Default serialization format not set")

    fun formatOfOrNull(mediaType: MediaType): SerializationFormat? =
        formatsMap[mediaType]

    fun formatOf(mediaType: MediaType): SerializationFormat =
        formatOfOrNull(mediaType) ?: error(formatNotFound(mediaType.fullType))

    private fun formatNotFound(fullType: String): String {
        val formatList = formats.joinToString(", ") { it.mediaType.fullType }
        return "Cannot find serialization format for: $fullType. Available: $formatList"
    }
}
