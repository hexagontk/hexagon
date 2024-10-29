package com.hexagontk.core.text

import com.hexagontk.core.info
import com.hexagontk.core.loggerOf
import com.hexagontk.core.text.Ansi.OSC
import com.hexagontk.core.text.Ansi.RESET
import com.hexagontk.core.text.Ansi.ST
import com.hexagontk.core.text.AnsiColor.BLACK
import com.hexagontk.core.text.AnsiColor.BLUE_BG
import com.hexagontk.core.text.AnsiEffect.UNDERLINE
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException
import java.lang.System.Logger
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class AnsiTest {

    private val logger: Logger by lazy { loggerOf(this::class) }

    @Test fun `Color edge values`() {
        println("${AnsiColor.bg(0, 0, 0)}${AnsiColor.fg(0, 0, 0)}TEST$RESET")
        println("${AnsiColor.bg(255, 255, 255)}${AnsiColor.fg(255, 255, 255)}TEST$RESET")
        assertEquals(
            "Red value must be in the 0..255 range: -1",
            assertFailsWith<IllegalArgumentException> { AnsiColor.bg(-1, 0, 0) }.message
        )
        assertEquals(
            "Red value must be in the 0..255 range: 256",
            assertFailsWith<IllegalArgumentException> { AnsiColor.bg(256, 0, 0) }.message
        )
        assertEquals(
            "Green value must be in the 0..255 range: -1",
            assertFailsWith<IllegalArgumentException> { AnsiColor.bg(0, -1, 0) }.message
        )
        assertEquals(
            "Green value must be in the 0..255 range: 256",
            assertFailsWith<IllegalArgumentException> { AnsiColor.bg(0, 256, 0) }.message
        )
        assertEquals(
            "Blue value must be in the 0..255 range: -1",
            assertFailsWith<IllegalArgumentException> { AnsiColor.bg(0, 0, -1) }.message
        )
        assertEquals(
            "Blue value must be in the 0..255 range: 256",
            assertFailsWith<IllegalArgumentException> { AnsiColor.bg(0, 0, 256) }.message
        )
    }

    @Test fun `True colors`() {
        fun background(r: Int, g: Int, b: Int) {
            val bg = AnsiColor.bg(r, g, b)
            print("${bg}X$RESET")
        }

        fun foreground(r: Int, g: Int, b: Int) {
            val fg = AnsiColor.fg(r, g, b)
            print("${fg}X$RESET")
        }

        for (r in 0..255 step 4)
            background(r, 0, 0)

        println()
        for (r in 0..255 step 4)
            background(0, r, 0)

        println()
        for (r in 0..255 step 4)
            background(0, 0, r)

        println()
        for (r in 0..255 step 4)
            foreground(r, 0, 0)

        println()
        for (r in 0..255 step 4)
            foreground(0, r, 0)

        println()
        for (r in 0..255 step 4)
            foreground(0, 0, r)
    }

    @Test fun `Switch terminal emulator title`() {
        print("${OSC}2;TEST$ST")
    }

    @Test fun `ANSI codes are printed properly`() {

        fun test(message: String) {
            logger.info { "${message}$RESET | normal text" }
        }

        test("${Ansi.CSI}30m black")
        test("${BLACK}black")
        test("${AnsiColor.RED}red")
        test("${AnsiColor.GREEN}green")
        test("${AnsiColor.YELLOW}yellow")
        test("${AnsiColor.BLUE}blue")
        test("${AnsiColor.MAGENTA}magenta")
        test("${AnsiColor.CYAN}cyan")
        test("${AnsiColor.WHITE}white")
        test("${AnsiColor.DEFAULT}default")

        test("${AnsiColor.BLACK_BG}${AnsiColor.BRIGHT_BLACK}black bg")
        test("${AnsiColor.RED_BG}${AnsiColor.BRIGHT_BLACK}red bg")
        test("${AnsiColor.GREEN_BG}${AnsiColor.BRIGHT_BLACK}green bg")
        test("${AnsiColor.YELLOW_BG}${AnsiColor.BRIGHT_BLACK}yellow bg")
        test("$BLUE_BG${AnsiColor.BRIGHT_BLACK}blue bg")
        test("${AnsiColor.MAGENTA_BG}${AnsiColor.BRIGHT_BLACK}magenta bg")
        test("${AnsiColor.CYAN_BG}${AnsiColor.BRIGHT_BLACK}cyan bg")
        test("${AnsiColor.WHITE_BG}${AnsiColor.BRIGHT_BLACK}white bg")
        test("${AnsiColor.DEFAULT_BG}${AnsiColor.BRIGHT_BLACK}default bg")

        test("${AnsiColor.BRIGHT_BLACK}bright black")
        test("${AnsiColor.BRIGHT_RED}bright red")
        test("${AnsiColor.BRIGHT_GREEN}bright green")
        test("${AnsiColor.BRIGHT_YELLOW}bright yellow")
        test("${AnsiColor.BRIGHT_BLUE}bright blue")
        test("${AnsiColor.BRIGHT_MAGENTA}bright magenta")
        test("${AnsiColor.BRIGHT_CYAN}bright cyan")
        test("${AnsiColor.BRIGHT_WHITE}bright white")

        test("${AnsiColor.BRIGHT_BLACK_BG}${AnsiColor.BRIGHT_BLACK}bright black bg")
        test("${AnsiColor.BRIGHT_RED_BG}${AnsiColor.BRIGHT_BLACK}bright red bg")
        test("${AnsiColor.BRIGHT_GREEN_BG}${AnsiColor.BRIGHT_BLACK}bright green bg")
        test("${AnsiColor.BRIGHT_YELLOW_BG}${AnsiColor.BRIGHT_BLACK}bright yellow bg")
        test("${AnsiColor.BRIGHT_BLUE_BG}${AnsiColor.BRIGHT_BLACK}bright blue bg")
        test("${AnsiColor.BRIGHT_MAGENTA_BG}${AnsiColor.BRIGHT_BLACK}bright magenta bg")
        test("${AnsiColor.BRIGHT_CYAN_BG}${AnsiColor.BRIGHT_BLACK}bright cyan bg")
        test("${AnsiColor.BRIGHT_WHITE_BG}${AnsiColor.BRIGHT_BLACK}bright white bg")

        test("${AnsiEffect.BOLD}bold")
        test("${UNDERLINE}underline")
        test("${AnsiEffect.BLINK}blink")
        test("${AnsiEffect.INVERSE}inverse")

        test("${AnsiEffect.BOLD_OFF}bold off")
        test("${AnsiEffect.UNDERLINE_OFF}underline off")
        test("${AnsiEffect.BLINK_OFF}blink off")
        test("${AnsiEffect.INVERSE_OFF}inverse off")

        test("${AnsiEffect.DIM}dim")
        test("${AnsiEffect.ITALIC}italic")
        test("${AnsiEffect.FAST_BLINK}fast blink")
        test("${AnsiEffect.STRIKE}strike")

        test("${AnsiEffect.DIM_OFF}dim off")
        test("${AnsiEffect.ITALIC_OFF}italic off")
        test("${AnsiEffect.STRIKE_OFF}strike off")

        test("$BLACK$BLUE_BG${UNDERLINE}black fg blue bg underline")
    }
}
