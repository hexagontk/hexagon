package com.hexagonkt.core.text

object AnsiEffect {
    /** Enable bold text. */
    const val BOLD = "${Ansi.CSI}1m"
    /** Enable dim text. */
    const val DIM = "${Ansi.CSI}2m"
    /** Enable italic text. */
    const val ITALIC = "${Ansi.CSI}3m"
    /** Enable underline text. */
    const val UNDERLINE = "${Ansi.CSI}4m"
    /** Enable blinking text. */
    const val BLINK = "${Ansi.CSI}5m"
    /** Enable fast blinking text. */
    const val FAST_BLINK = "${Ansi.CSI}6m"
    /** Enable inverse color text. */
    const val INVERSE = "${Ansi.CSI}7m"
    /** Enable strike text. */
    const val STRIKE = "${Ansi.CSI}9m"

    /** Disable bold text. */
    const val BOLD_OFF = "${Ansi.CSI}21m"
    /** Disable dim text. */
    const val DIM_OFF = "${Ansi.CSI}22m"
    /** Disable italic text. */
    const val ITALIC_OFF = "${Ansi.CSI}23m"
    /** Disable underline text. */
    const val UNDERLINE_OFF = "${Ansi.CSI}24m"
    /** Disable blinking text. */
    const val BLINK_OFF = "${Ansi.CSI}25m"
    /** Disable inverse color text. */
    const val INVERSE_OFF = "${Ansi.CSI}27m"
    /** Disable strike text. */
    const val STRIKE_OFF = "${Ansi.CSI}29m"
}
