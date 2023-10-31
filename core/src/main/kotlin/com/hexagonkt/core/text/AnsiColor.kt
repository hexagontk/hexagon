package com.hexagonkt.core.text

// TODO Add RGB colors
object AnsiColor {
    /** Set black as the foreground color. */
    const val BLACK = "${Ansi.CSI}30m"
    /** Set red as the foreground color. */
    const val RED = "${Ansi.CSI}31m"
    /** Set green as the foreground color. */
    const val GREEN = "${Ansi.CSI}32m"
    /** Set yellow as the foreground color. */
    const val YELLOW = "${Ansi.CSI}33m"
    /** Set blue as the foreground color. */
    const val BLUE = "${Ansi.CSI}34m"
    /** Set magenta as the foreground color. */
    const val MAGENTA = "${Ansi.CSI}35m"
    /** Set cyan as the foreground color. */
    const val CYAN = "${Ansi.CSI}36m"
    /** Set white as the foreground color. */
    const val WHITE = "${Ansi.CSI}37m"
    /** Set back the default foreground color. */
    const val DEFAULT = "${Ansi.CSI}39m"

    /** Set black as the background color. */
    const val BLACK_BG = "${Ansi.CSI}40m"
    /** Set red as the background color. */
    const val RED_BG = "${Ansi.CSI}41m"
    /** Set green as the background color. */
    const val GREEN_BG = "${Ansi.CSI}42m"
    /** Set yellow as the background color. */
    const val YELLOW_BG = "${Ansi.CSI}43m"
    /** Set blue as the background color. */
    const val BLUE_BG = "${Ansi.CSI}44m"
    /** Set magenta as the background color. */
    const val MAGENTA_BG = "${Ansi.CSI}45m"
    /** Set cyan as the background color. */
    const val CYAN_BG = "${Ansi.CSI}46m"
    /** Set white as the background color. */
    const val WHITE_BG = "${Ansi.CSI}47m"
    /** Set back the default background color. */
    const val DEFAULT_BG = "${Ansi.CSI}49m"

    /** Set bright black as the foreground color. */
    const val BRIGHT_BLACK = "${Ansi.CSI}90m"
    /** Set bright red as the foreground color. */
    const val BRIGHT_RED = "${Ansi.CSI}91m"
    /** Set bright green as the foreground color. */
    const val BRIGHT_GREEN = "${Ansi.CSI}92m"
    /** Set bright yellow as the foreground color. */
    const val BRIGHT_YELLOW = "${Ansi.CSI}93m"
    /** Set bright blue as the foreground color. */
    const val BRIGHT_BLUE = "${Ansi.CSI}94m"
    /** Set bright magenta as the foreground color. */
    const val BRIGHT_MAGENTA = "${Ansi.CSI}95m"
    /** Set bright cyan as the foreground color. */
    const val BRIGHT_CYAN = "${Ansi.CSI}96m"
    /** Set bright white as the foreground color. */
    const val BRIGHT_WHITE = "${Ansi.CSI}97m"

    /** Set bright black as the background color. */
    const val BRIGHT_BLACK_BG = "${Ansi.CSI}100m"
    /** Set bright red as the background color. */
    const val BRIGHT_RED_BG = "${Ansi.CSI}101m"
    /** Set bright green as the background color. */
    const val BRIGHT_GREEN_BG = "${Ansi.CSI}102m"
    /** Set bright yellow as the background color. */
    const val BRIGHT_YELLOW_BG = "${Ansi.CSI}103m"
    /** Set bright blue as the background color. */
    const val BRIGHT_BLUE_BG = "${Ansi.CSI}104m"
    /** Set bright magenta as the background color. */
    const val BRIGHT_MAGENTA_BG = "${Ansi.CSI}105m"
    /** Set bright cyan as the background color. */
    const val BRIGHT_CYAN_BG = "${Ansi.CSI}106m"
    /** Set bright white as the background color. */
    const val BRIGHT_WHITE_BG = "${Ansi.CSI}107m"
}
