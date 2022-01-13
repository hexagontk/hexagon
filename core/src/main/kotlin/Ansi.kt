package com.hexagonkt.core

/**
 * Constants for console formatting with [ANSI](https://en.wikipedia.org/wiki/ANSI_escape_code)
 * codes. They can be used in strings to enable or disable a display option.
 */
object Ansi {
    /** Disable all options applied before. */
    const val RESET = "\u001B[0m"

    /** Set black as the foreground color. */
    const val BLACK = "\u001B[30m"
    /** Set red as the foreground color. */
    const val RED = "\u001B[31m"
    /** Set green as the foreground color. */
    const val GREEN = "\u001B[32m"
    /** Set yellow as the foreground color. */
    const val YELLOW = "\u001B[33m"
    /** Set blue as the foreground color. */
    const val BLUE = "\u001B[34m"
    /** Set magenta as the foreground color. */
    const val MAGENTA = "\u001B[35m"
    /** Set cyan as the foreground color. */
    const val CYAN = "\u001B[36m"
    /** Set white as the foreground color. */
    const val WHITE = "\u001B[37m"
    /** Set back the default foreground color. */
    const val DEFAULT = "\u001B[39m"

    /** Set black as the background color. */
    const val BLACK_BG = "\u001B[40m"
    /** Set red as the background color. */
    const val RED_BG = "\u001B[41m"
    /** Set green as the background color. */
    const val GREEN_BG = "\u001B[42m"
    /** Set yellow as the background color. */
    const val YELLOW_BG = "\u001B[43m"
    /** Set blue as the background color. */
    const val BLUE_BG = "\u001B[44m"
    /** Set magenta as the background color. */
    const val MAGENTA_BG = "\u001B[45m"
    /** Set cyan as the background color. */
    const val CYAN_BG = "\u001B[46m"
    /** Set white as the background color. */
    const val WHITE_BG = "\u001B[47m"
    /** Set back the default background color. */
    const val DEFAULT_BG = "\u001B[49m"

    /** Set bright black as the foreground color. */
    const val BRIGHT_BLACK = "\u001B[90m"
    /** Set bright red as the foreground color. */
    const val BRIGHT_RED = "\u001B[91m"
    /** Set bright green as the foreground color. */
    const val BRIGHT_GREEN = "\u001B[92m"
    /** Set bright yellow as the foreground color. */
    const val BRIGHT_YELLOW = "\u001B[93m"
    /** Set bright blue as the foreground color. */
    const val BRIGHT_BLUE = "\u001B[94m"
    /** Set bright magenta as the foreground color. */
    const val BRIGHT_MAGENTA = "\u001B[95m"
    /** Set bright cyan as the foreground color. */
    const val BRIGHT_CYAN = "\u001B[96m"
    /** Set bright white as the foreground color. */
    const val BRIGHT_WHITE = "\u001B[97m"

    /** Set bright black as the background color. */
    const val BRIGHT_BLACK_BG = "\u001B[100m"
    /** Set bright red as the background color. */
    const val BRIGHT_RED_BG = "\u001B[101m"
    /** Set bright green as the background color. */
    const val BRIGHT_GREEN_BG = "\u001B[102m"
    /** Set bright yellow as the background color. */
    const val BRIGHT_YELLOW_BG = "\u001B[103m"
    /** Set bright blue as the background color. */
    const val BRIGHT_BLUE_BG = "\u001B[104m"
    /** Set bright magenta as the background color. */
    const val BRIGHT_MAGENTA_BG = "\u001B[105m"
    /** Set bright cyan as the background color. */
    const val BRIGHT_CYAN_BG = "\u001B[106m"
    /** Set bright white as the background color. */
    const val BRIGHT_WHITE_BG = "\u001B[107m"

    /** Enable bold text. */
    const val BOLD = "\u001B[1m"
    /** Enable underline text. */
    const val UNDERLINE = "\u001B[4m"
    /** Enable blinking text. */
    const val BLINK = "\u001B[5m"
    /** Enable inverse color text. */
    const val INVERSE = "\u001B[7m"

    /** Disable bold text. */
    const val BOLD_OFF = "\u001B[21m"
    /** Disable underline text. */
    const val UNDERLINE_OFF = "\u001B[24m"
    /** Disable blinking text. */
    const val BLINK_OFF = "\u001B[25m"
    /** Disable inverse color text. */
    const val INVERSE_OFF = "\u001B[27m"
}
