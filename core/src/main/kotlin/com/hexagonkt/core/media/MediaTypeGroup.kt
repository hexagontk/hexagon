package com.hexagonkt.core.media

enum class MediaTypeGroup {
    APPLICATION,
    AUDIO,
    FONT,
    IMAGE,
    MULTIPART,
    TEXT,
    VIDEO;

    val text: String = toString().lowercase()
}
