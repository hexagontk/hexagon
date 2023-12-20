package com.hexagonkt.core.text

/**
 * Constants for console formatting with [ANSI](https://en.wikipedia.org/wiki/ANSI_escape_code)
 * codes. They can be used in strings to enable or disable a display option.
 */
object Ansi {
    /** Regex that matches ANSI escape sequences. */
    val REGEX: Regex by lazy { """\u001B\[\d+?m""".toRegex() }

    /** Control Sequence Introducer. */
    const val CSI = "\u001B["
    /** Operating System Command. */
    const val OSC = "\u001B]"
    /** String Terminator. */
    const val ST = "\u001B\\"

    /** Disable all options applied before. */
    const val RESET = "${CSI}0m"
}
