package com.hexagonkt.core.media

/**
 * Prebuilt audio media types. Only validated once at start time (not constructed each time).
 */
enum class AudioMedia(
    override val group: MediaTypeGroup,
    override val type: String,
    override val fullType: String = "${group.text}/$type"
) : MediaType {

    BASIC(MediaTypeGroup.AUDIO, "basic"),
    MPEG(MediaTypeGroup.AUDIO, "mpeg"),
    WAV(MediaTypeGroup.AUDIO, "wav"),

    AAC(MediaTypeGroup.AUDIO, "aac"),
    MIDI(MediaTypeGroup.AUDIO, "midi"),
    OGG(MediaTypeGroup.AUDIO, "ogg"),
    OPUS(MediaTypeGroup.AUDIO, "opus"),
    WEBM(MediaTypeGroup.AUDIO, "webm"),
}
