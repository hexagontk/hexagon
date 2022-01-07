package com.hexagonkt.core.media

/**
 * Prebuilt font media types. Only validated once at start time (not constructed each time).
 */
enum class FontMedia(
    override val group: MediaTypeGroup,
    override val type: String,
    override val fullType: String = "${group.text}/$type"
) : MediaType {

    OTF(MediaTypeGroup.FONT, "otf"),
    TTF(MediaTypeGroup.FONT, "ttf"),
    WOFF(MediaTypeGroup.FONT, "woff"),
    WOFF2(MediaTypeGroup.FONT, "woff2"),
}
