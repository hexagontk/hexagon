package com.hexagontk.terminal

import com.hexagontk.core.text.Ansi.CSI

/**
 * Constants for console cursor handling with [ANSI](https://en.wikipedia.org/wiki/ANSI_escape_code)
 * codes.
 *
 * TODO https://invisible-island.net/xterm/ctlseqs/ctlseqs.html
 */
object AnsiCursor {
    /** Move cursor to origin. */
    const val HOME: String = "${CSI}H"
    const val SAVE: String = "\u001B${'7'}"
    const val RESTORE: String = "\u001B${'8'}"
    const val SHOW: String = "$CSI?25h"
    const val HIDE: String = "$CSI?25l"
    const val PRINT: String = "${CSI}6n"

    fun position(r: Int = 1, c: Int = 1): String =
        "$CSI${r};${c}H"

    fun up(d: Int = 1): String =
        "$CSI${d}A"

    fun down(d: Int = 1): String =
        "$CSI${d}B"

    fun forward(d: Int = 1): String =
        "$CSI${d}C"

    fun backward(d: Int = 1): String =
        "$CSI${d}D"
}
