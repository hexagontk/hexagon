package com.hexagonkt.core

/**
 * Constants for console formatting with [ANSI](https://en.wikipedia.org/wiki/ANSI_escape_code)
 * codes. They can be used in strings to enable or disable a display option.
 */
object Ansi {
    /** Regex that matches ANSI escape sequences. */
    val regex: Regex = """\u001B\[\d+?m""".toRegex()

    /** Control Sequence Introducer. */
    const val CSI = "\u001B["

    /** Disable all options applied before. */
    const val RESET = "${CSI}0m"

    /** Set black as the foreground color. */
    const val BLACK = "${CSI}30m"
    /** Set red as the foreground color. */
    const val RED = "${CSI}31m"
    /** Set green as the foreground color. */
    const val GREEN = "${CSI}32m"
    /** Set yellow as the foreground color. */
    const val YELLOW = "${CSI}33m"
    /** Set blue as the foreground color. */
    const val BLUE = "${CSI}34m"
    /** Set magenta as the foreground color. */
    const val MAGENTA = "${CSI}35m"
    /** Set cyan as the foreground color. */
    const val CYAN = "${CSI}36m"
    /** Set white as the foreground color. */
    const val WHITE = "${CSI}37m"
    /** Set back the default foreground color. */
    const val DEFAULT = "${CSI}39m"

    /** Set black as the background color. */
    const val BLACK_BG = "${CSI}40m"
    /** Set red as the background color. */
    const val RED_BG = "${CSI}41m"
    /** Set green as the background color. */
    const val GREEN_BG = "${CSI}42m"
    /** Set yellow as the background color. */
    const val YELLOW_BG = "${CSI}43m"
    /** Set blue as the background color. */
    const val BLUE_BG = "${CSI}44m"
    /** Set magenta as the background color. */
    const val MAGENTA_BG = "${CSI}45m"
    /** Set cyan as the background color. */
    const val CYAN_BG = "${CSI}46m"
    /** Set white as the background color. */
    const val WHITE_BG = "${CSI}47m"
    /** Set back the default background color. */
    const val DEFAULT_BG = "${CSI}49m"

    /** Set bright black as the foreground color. */
    const val BRIGHT_BLACK = "${CSI}90m"
    /** Set bright red as the foreground color. */
    const val BRIGHT_RED = "${CSI}91m"
    /** Set bright green as the foreground color. */
    const val BRIGHT_GREEN = "${CSI}92m"
    /** Set bright yellow as the foreground color. */
    const val BRIGHT_YELLOW = "${CSI}93m"
    /** Set bright blue as the foreground color. */
    const val BRIGHT_BLUE = "${CSI}94m"
    /** Set bright magenta as the foreground color. */
    const val BRIGHT_MAGENTA = "${CSI}95m"
    /** Set bright cyan as the foreground color. */
    const val BRIGHT_CYAN = "${CSI}96m"
    /** Set bright white as the foreground color. */
    const val BRIGHT_WHITE = "${CSI}97m"

    /** Set bright black as the background color. */
    const val BRIGHT_BLACK_BG = "${CSI}100m"
    /** Set bright red as the background color. */
    const val BRIGHT_RED_BG = "${CSI}101m"
    /** Set bright green as the background color. */
    const val BRIGHT_GREEN_BG = "${CSI}102m"
    /** Set bright yellow as the background color. */
    const val BRIGHT_YELLOW_BG = "${CSI}103m"
    /** Set bright blue as the background color. */
    const val BRIGHT_BLUE_BG = "${CSI}104m"
    /** Set bright magenta as the background color. */
    const val BRIGHT_MAGENTA_BG = "${CSI}105m"
    /** Set bright cyan as the background color. */
    const val BRIGHT_CYAN_BG = "${CSI}106m"
    /** Set bright white as the background color. */
    const val BRIGHT_WHITE_BG = "${CSI}107m"

    /** Enable bold text. */
    const val BOLD = "${CSI}1m"
    /** Enable underline text. */
    const val UNDERLINE = "${CSI}4m"
    /** Enable blinking text. */
    const val BLINK = "${CSI}5m"
    /** Enable inverse color text. */
    const val INVERSE = "${CSI}7m"

    /** Disable bold text. */
    const val BOLD_OFF = "${CSI}21m"
    /** Disable underline text. */
    const val UNDERLINE_OFF = "${CSI}24m"
    /** Disable blinking text. */
    const val BLINK_OFF = "${CSI}25m"
    /** Disable inverse color text. */
    const val INVERSE_OFF = "${CSI}27m"
}
