package com.hexagonkt.helpers

object Ansi {
    const val RESET = "\u001B[0m"

    const val BLACK_FG = "\u001B[30m"
    const val RED_FG = "\u001B[31m"
    const val GREEN_FG = "\u001B[32m"
    const val YELLOW_FG = "\u001B[33m"
    const val BLUE_FG = "\u001B[34m"
    const val MAGENTA_FG = "\u001B[35m"
    const val CYAN_FG = "\u001B[36m"
    const val WHITE_FG = "\u001B[37m"
    const val DEFAULT_FG = "\u001B[39m"

    const val BLACK_BG = "\u001B[40m"
    const val RED_BG = "\u001B[41m"
    const val GREEN_BG = "\u001B[42m"
    const val YELLOW_BG = "\u001B[43m"
    const val BLUE_BG = "\u001B[44m"
    const val MAGENTA_BG = "\u001B[45m"
    const val CYAN_BG = "\u001B[46m"
    const val WHITE_BG = "\u001B[47m"
    const val DEFAULT_BG = "\u001B[49m"

    const val BOLD_ON = "\u001B[1m"
    const val UNDERLINE_ON = "\u001B[4m"
    const val BLINK_ON = "\u001B[5m"
    const val INVERSE_ON = "\u001B[7m"

    const val BOLD_OFF = "\u001B[21m"
    const val UNDERLINE_OFF = "\u001B[24m"
    const val BLINK_OFF = "\u001B[25m"
    const val INVERSE_OFF = "\u001B[27m"
}
