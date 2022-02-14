package com.hexagonkt.core.media

/**
 * Prebuilt image media types. Only validated once at start time (not constructed each time).
 */
enum class ImageMedia(
    override val group: MediaTypeGroup,
    override val type: String,
    override val fullType: String = "${group.text}/$type"
) : MediaType {

    GIF(MediaTypeGroup.IMAGE, "gif"),
    JPEG(MediaTypeGroup.IMAGE, "jpeg"),
    PNG(MediaTypeGroup.IMAGE, "png"),
    TIFF(MediaTypeGroup.IMAGE, "tiff"),
    SVG(MediaTypeGroup.IMAGE, "svg+xml"),
    ICO(MediaTypeGroup.IMAGE, "vnd.microsoft.icon"),
    WEBP(MediaTypeGroup.IMAGE, "webp"),
}
