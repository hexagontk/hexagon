package com.hexagontk.terminal

import com.hexagontk.helpers.exec
import java.lang.System.getenv
import java.lang.management.ManagementFactory.getRuntimeMXBean
import kotlin.Int.Companion.MAX_VALUE

/**
 * TODO Check:
 *  https://wezfurlong.org/wezterm/escape-sequences.html#operating-system-command-sequences
 *  https://docs.google.com/spreadsheets/d/19W-lXWS9jYwqCK-LwgYo31GucPPxYVld_hVEcfpNpXg/edit#gid=1724051764
 *  https://github.com/JakeWharton/mosaic
 */
object Terminal {
    private val pid: Long by lazy { getRuntimeMXBean().pid }
    private val pts: String by lazy { getenv("PTS") ?: "ps o tty= $pid".exec() }

    fun raw() {
        "stty raw -echo -F/dev/$pts".exec()
    }

    fun cooked() {
        "stty cooked echo -F/dev/$pts".exec()
    }

    fun size(): Pair<Int, Int> {
        print(AnsiCursor.SAVE)
        print(AnsiCursor.position(MAX_VALUE, MAX_VALUE))
        print(AnsiCursor.PRINT)
        val r = System.`in`
        val builder = StringBuilder(8)
        while (true) {
            val c = r.read().toChar()
            if (c == 'R')
                break
            else
                if (c !in setOf('[', '\u001B'))
                    builder.append(c)
        }
        print(AnsiCursor.RESTORE)
        return builder.split(';').map { it.toInt() }.let { it.first() to it.last() }
    }

    fun readEvents() {
        val r = System.`in`
        while (true) {
            when (val c = r.read().toChar()) {
                'q' -> break
                '\u001B' -> print("ESC")
                else -> print(c)
            }
        }
    }
}
