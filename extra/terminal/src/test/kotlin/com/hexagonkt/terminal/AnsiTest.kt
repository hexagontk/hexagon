package com.hexagonkt.terminal

import kotlin.test.Test

internal class AnsiTest {

    @Test fun `ANSI control codes are printed properly`() {
        print("DEMO")
        print(AnsiScreen.privateMode())
        print(AnsiCursor.HOME)
        print(AnsiCursor.SAVE)
        print("Start")
        print(AnsiCursor.position(5, 10))
        print("End")
        print(AnsiCursor.RESTORE)
        print("Overwrite")
        print(AnsiScreen.privateMode(false))
        print("END")
    }
}
