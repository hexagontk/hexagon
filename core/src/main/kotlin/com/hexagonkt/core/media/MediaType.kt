package com.hexagonkt.core.media

/**
 * Also known as MIME type.
 */
interface MediaType {
    val group: MediaTypeGroup
    val type: String
    val fullType: String

    companion object {
        val fullTypes: Map<String, MediaType> =
            listOf(
                ApplicationMedia.values(),
                AudioMedia.values(),
                FontMedia.values(),
                ImageMedia.values(),
                MultipartMedia.values(),
                TextMedia.values(),
                VideoMedia.values(),
            )
            .flatMap { it.toList() }
            .associateBy { it.fullType }

        operator fun get(extension: String): MediaType =
            extensions[extension] ?: defaultMediaType

        operator fun invoke(fullType: String): MediaType =
            fullTypes[fullType] ?: parseMediaType(fullType)
    }
}
