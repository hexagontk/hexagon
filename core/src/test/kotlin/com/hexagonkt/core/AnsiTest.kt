package com.hexagonkt.core

import com.hexagonkt.core.logging.Logger
import org.junit.jupiter.api.Test

internal class AnsiTest {

    private val logger: Logger by lazy { Logger(this::class) }

    @Test fun `ANSI codes are printed properly`() {

        fun test(message: String) {
            logger.info { "${message}${Ansi.RESET} | normal text" }
        }

        test("${Ansi.CSI}30m black")
        test("${Ansi.BLACK}black")
        test("${Ansi.RED}red")
        test("${Ansi.GREEN}green")
        test("${Ansi.YELLOW}yellow")
        test("${Ansi.BLUE}blue")
        test("${Ansi.MAGENTA}magenta")
        test("${Ansi.CYAN}cyan")
        test("${Ansi.WHITE}white")
        test("${Ansi.DEFAULT}default")

        test("${Ansi.BLACK_BG}${Ansi.BRIGHT_BLACK}black bg")
        test("${Ansi.RED_BG}${Ansi.BRIGHT_BLACK}red bg")
        test("${Ansi.GREEN_BG}${Ansi.BRIGHT_BLACK}green bg")
        test("${Ansi.YELLOW_BG}${Ansi.BRIGHT_BLACK}yellow bg")
        test("${Ansi.BLUE_BG}${Ansi.BRIGHT_BLACK}blue bg")
        test("${Ansi.MAGENTA_BG}${Ansi.BRIGHT_BLACK}magenta bg")
        test("${Ansi.CYAN_BG}${Ansi.BRIGHT_BLACK}cyan bg")
        test("${Ansi.WHITE_BG}${Ansi.BRIGHT_BLACK}white bg")
        test("${Ansi.DEFAULT_BG}${Ansi.BRIGHT_BLACK}default bg")

        test("${Ansi.BRIGHT_BLACK}bright black")
        test("${Ansi.BRIGHT_RED}bright red")
        test("${Ansi.BRIGHT_GREEN}bright green")
        test("${Ansi.BRIGHT_YELLOW}bright yellow")
        test("${Ansi.BRIGHT_BLUE}bright blue")
        test("${Ansi.BRIGHT_MAGENTA}bright magenta")
        test("${Ansi.BRIGHT_CYAN}bright cyan")
        test("${Ansi.BRIGHT_WHITE}bright white")

        test("${Ansi.BRIGHT_BLACK_BG}${Ansi.BRIGHT_BLACK}bright black bg")
        test("${Ansi.BRIGHT_RED_BG}${Ansi.BRIGHT_BLACK}bright red bg")
        test("${Ansi.BRIGHT_GREEN_BG}${Ansi.BRIGHT_BLACK}bright green bg")
        test("${Ansi.BRIGHT_YELLOW_BG}${Ansi.BRIGHT_BLACK}bright yellow bg")
        test("${Ansi.BRIGHT_BLUE_BG}${Ansi.BRIGHT_BLACK}bright blue bg")
        test("${Ansi.BRIGHT_MAGENTA_BG}${Ansi.BRIGHT_BLACK}bright magenta bg")
        test("${Ansi.BRIGHT_CYAN_BG}${Ansi.BRIGHT_BLACK}bright cyan bg")
        test("${Ansi.BRIGHT_WHITE_BG}${Ansi.BRIGHT_BLACK}bright white bg")

        test("${Ansi.BOLD}bold")
        test("${Ansi.UNDERLINE}underline")
        test("${Ansi.BLINK}blink")
        test("${Ansi.INVERSE}inverse")

        test("${Ansi.BOLD_OFF}bold off")
        test("${Ansi.UNDERLINE_OFF}underline off")
        test("${Ansi.BLINK_OFF}blink off")
        test("${Ansi.INVERSE_OFF}inverse off")

        test("${Ansi.BLACK}${Ansi.BLUE_BG}${Ansi.UNDERLINE}black fg blue bg underline")
    }
}
