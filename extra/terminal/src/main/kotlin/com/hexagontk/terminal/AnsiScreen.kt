package com.hexagontk.terminal

import com.hexagontk.core.text.Ansi.CSI
import com.hexagontk.terminal.AnsiScreen.Region.FULL

/**
 * Constants for console cursor handling with [ANSI](https://en.wikipedia.org/wiki/ANSI_escape_code)
 * codes.
 */
object AnsiScreen {

    enum class Region { END, START, FULL, FULL_BUFFER }

    fun clear(flag: Region = FULL): String =
        "$CSI${flag.ordinal}J"

    fun lineClear(flag: Region = FULL): String =
        "$CSI${flag.ordinal}K"

    fun privateMode(flag: Boolean = true): String =
        "$CSI?1049${if (flag) 'h' else 'l'}"
}
