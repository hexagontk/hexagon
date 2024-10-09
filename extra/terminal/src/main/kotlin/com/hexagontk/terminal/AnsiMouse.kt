package com.hexagontk.terminal

import com.hexagontk.core.text.Ansi.CSI

/**
 * See https://github.com/kovidgoyal/kitty/commit/6777e2199e7b0ed011b2888ce94ad6abab3a5ced
 * TODO https://sw.kovidgoyal.net/kitty/pointer-shapes/#
 */
object AnsiMouse {
    const val ENABLE: String = "${CSI}?1003h"
    const val DISABLE: String = "${CSI}?1003l"
    const val ENABLE_SGR: String = "${CSI}?1006h"
    const val DISABLE_SGR: String = "${CSI}?1006l"
}
