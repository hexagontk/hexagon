package com.hexagonkt.core.media

/**
 * Prebuilt text media types. Only validated once at start time (not constructed each time).
 */
enum class TextMedia(
    override val group: MediaTypeGroup,
    override val type: String,
    override val fullType: String = "${group.text}/$type"
) : MediaType {

    CSV(MediaTypeGroup.TEXT, "csv"),
    X_JAVA_PROPERTIES(MediaTypeGroup.TEXT, "x-java-properties"),
    JAVASCRIPT(MediaTypeGroup.TEXT, "javascript"),
    CSS(MediaTypeGroup.TEXT, "css"),
    HTML(MediaTypeGroup.TEXT, "html"),
    MARKDOWN(MediaTypeGroup.TEXT, "markdown"),
    PLAIN(MediaTypeGroup.TEXT, "plain"),
    RICHTEXT(MediaTypeGroup.TEXT, "richtext"),
    TAB_SEPARATED_VALUES(MediaTypeGroup.TEXT, "tab-separated-values"),
    CALENDAR(MediaTypeGroup.TEXT, "calendar"),
    EVENT_STREAM(MediaTypeGroup.TEXT, "event-stream"),
}
