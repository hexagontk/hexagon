package com.hexagonkt.core.text

object AnsiEffect {
    /** Enable bold text. */
    const val BOLD = "${Ansi.CSI}1m"
    /** Enable underline text. */
    const val UNDERLINE = "${Ansi.CSI}4m"
    /** Enable blinking text. */
    const val BLINK = "${Ansi.CSI}5m"
    /** Enable inverse color text. */
    const val INVERSE = "${Ansi.CSI}7m"

    /** Disable bold text. */
    const val BOLD_OFF = "${Ansi.CSI}21m"
    /** Disable underline text. */
    const val UNDERLINE_OFF = "${Ansi.CSI}24m"
    /** Disable blinking text. */
    const val BLINK_OFF = "${Ansi.CSI}25m"
    /** Disable inverse color text. */
    const val INVERSE_OFF = "${Ansi.CSI}27m"
}
