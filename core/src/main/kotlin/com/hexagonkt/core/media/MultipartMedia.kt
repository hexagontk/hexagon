package com.hexagonkt.core.media

/**
 * Prebuilt multipart media types. Only validated once at start time (not constructed each time).
 */
enum class MultipartMedia(
    override val group: MediaTypeGroup,
    override val type: String,
    override val fullType: String = "${group.text}/$type"
) : MediaType {

    ALTERNATIVE(MediaTypeGroup.MULTIPART, "alternative"),
    APPLEDOUBLE(MediaTypeGroup.MULTIPART, "appledouble"),
    DIGEST(MediaTypeGroup.MULTIPART, "digest"),
    MIXED(MediaTypeGroup.MULTIPART, "mixed"),
    PARALLEL(MediaTypeGroup.MULTIPART, "parallel"),
}
