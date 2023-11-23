package com.hexagonkt.core.text

import com.hexagonkt.core.logging.Logger
import com.hexagonkt.core.text.Ansi.OSC
import com.hexagonkt.core.text.Ansi.ST
import com.hexagonkt.core.text.AnsiColor.BLACK
import com.hexagonkt.core.text.AnsiColor.BLUE_BG
import com.hexagonkt.core.text.AnsiEffect.UNDERLINE
import org.junit.jupiter.api.Test

internal class AnsiTest {

    private val logger: Logger by lazy { Logger(this::class) }

    @Test fun `True colors`() {
        fun background(r: Int, g: Int, b: Int) {
            val bg = AnsiColor.bg(r.toByte(), g.toByte(), b.toByte())
            print("${bg}X${Ansi.RESET}")
        }

        fun foreground(r: Int, g: Int, b: Int) {
            val fg = AnsiColor.fg(r.toByte(), g.toByte(), b.toByte())
            print("${fg}X${Ansi.RESET}")
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
            logger.info { "${message}${Ansi.RESET} | normal text" }
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
