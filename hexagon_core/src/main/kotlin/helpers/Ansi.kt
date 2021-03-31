package com.hexagonkt.helpers

/**
 * Constants for console formatting with [ANSI](https://en.wikipedia.org/wiki/ANSI_escape_code)
 * codes.
 *
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 */
object Ansi {
    const val RESET = "\u001B[0m"

    const val BLACK = "\u001B[30m"
    const val RED = "\u001B[31m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val MAGENTA = "\u001B[35m"
    const val CYAN = "\u001B[36m"
    const val WHITE = "\u001B[37m"
    const val DEFAULT = "\u001B[39m"

    const val BLACK_BG = "\u001B[40m"
    const val RED_BG = "\u001B[41m"
    const val GREEN_BG = "\u001B[42m"
    const val YELLOW_BG = "\u001B[43m"
    const val BLUE_BG = "\u001B[44m"
    const val MAGENTA_BG = "\u001B[45m"
    const val CYAN_BG = "\u001B[46m"
    const val WHITE_BG = "\u001B[47m"
    const val DEFAULT_BG = "\u001B[49m"

    const val BRIGHT_BLACK = "\u001B[90m"
    const val BRIGHT_RED = "\u001B[91m"
    const val BRIGHT_GREEN = "\u001B[92m"
    const val BRIGHT_YELLOW = "\u001B[93m"
    const val BRIGHT_BLUE = "\u001B[94m"
    const val BRIGHT_MAGENTA = "\u001B[95m"
    const val BRIGHT_CYAN = "\u001B[96m"
    const val BRIGHT_WHITE = "\u001B[97m"

    const val BRIGHT_BLACK_BG = "\u001B[100m"
    const val BRIGHT_RED_BG = "\u001B[101m"
    const val BRIGHT_GREEN_BG = "\u001B[102m"
    const val BRIGHT_YELLOW_BG = "\u001B[103m"
    const val BRIGHT_BLUE_BG = "\u001B[104m"
    const val BRIGHT_MAGENTA_BG = "\u001B[105m"
    const val BRIGHT_CYAN_BG = "\u001B[106m"
    const val BRIGHT_WHITE_BG = "\u001B[107m"

    const val BOLD = "\u001B[1m"
    const val UNDERLINE = "\u001B[4m"
    const val BLINK = "\u001B[5m"
    const val INVERSE = "\u001B[7m"

    const val BOLD_OFF = "\u001B[21m"
    const val UNDERLINE_OFF = "\u001B[24m"
    const val BLINK_OFF = "\u001B[25m"
    const val INVERSE_OFF = "\u001B[27m"
}
