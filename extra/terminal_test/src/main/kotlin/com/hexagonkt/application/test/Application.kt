package com.hexagonkt.application.test

import com.hexagonkt.terminal.AnsiCursor
import com.hexagonkt.terminal.AnsiMouse
import com.hexagonkt.terminal.AnsiScreen
import com.hexagonkt.terminal.Terminal
import java.io.InputStream
import java.lang.StringBuilder

fun main() {
    terminalTest()
}

private fun terminalTest() {
    Terminal.raw()
    print(AnsiScreen.privateMode())
    print(AnsiCursor.HIDE)
    print(AnsiCursor.HOME)
    print(Terminal.size())
    print(AnsiMouse.ENABLE)
    print(AnsiMouse.ENABLE_SGR)

    val r = System.`in`
    while (true) {
        print(AnsiCursor.position(2, 0))
        val c = r.read().toChar()
        print(AnsiScreen.lineClear())
        when (c) {
            'q' -> break
            '\u001B' -> escape(r)
            else -> print(c)
        }
    }

    print(AnsiMouse.DISABLE_SGR)
    print(AnsiMouse.DISABLE)
    print(AnsiScreen.privateMode(false))
    print(AnsiCursor.HOME)
    print(AnsiCursor.SHOW)
    print(AnsiScreen.clear())
    Terminal.cooked()
}

fun escape(i: InputStream) {
    when (i.read().toChar()) {
        '[' -> code(i)
        else -> print("ESC")
    }
}

fun code(i: InputStream) {
    when (i.read().toChar()) {
        'A' -> print(" up ")
        'B' -> print(" down ")
        'C' -> print(" right ")
        'D' -> print(" left ")
        'M' -> mouse(i)
        '<' -> mouseSgr(i)
        else -> print("CODE")
    }
}

fun mouse(i: InputStream) {
    val b = StringBuilder(3)
    b.append(i.read())
    b.append(", ")
    b.append(i.read() - 32)
    b.append(", ")
    b.append(i.read() - 32)

    print("MOUSE($b)")
}

fun mouseSgr(i: InputStream) {
    val b = StringBuilder()
    while (true) {
        val c = i.read().toChar()
        b.append(c)
        if (c in setOf('m', 'M'))
             break
    }

    print("MOUSE_SGR($b)")
}
