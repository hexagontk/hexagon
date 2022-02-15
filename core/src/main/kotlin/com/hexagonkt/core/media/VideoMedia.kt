package com.hexagonkt.core.media

/**
 * Prebuilt video media types. Only validated once at start time (not constructed each time).
 */
enum class VideoMedia(
    override val group: MediaTypeGroup,
    override val type: String,
    override val fullType: String = "${group.text}/$type"
) : MediaType {

    MPEG(MediaTypeGroup.VIDEO, "mpeg"),
    QUICKTIME(MediaTypeGroup.VIDEO, "quicktime"),
    X_MSVIDEO(MediaTypeGroup.VIDEO, "x-msvideo"),

    MP4(MediaTypeGroup.VIDEO, "mp4"),
    OGG(MediaTypeGroup.VIDEO, "ogg"),
    WEBM(MediaTypeGroup.VIDEO, "webm"),
}
