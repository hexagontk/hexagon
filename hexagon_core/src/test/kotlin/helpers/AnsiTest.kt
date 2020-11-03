package helpers

import com.hexagonkt.helpers.Ansi
import com.hexagonkt.logging.Logger
import org.junit.jupiter.api.Test

internal class AnsiTest {

    private val logger: Logger by lazy { Logger(this::class) }

    @Test fun `ANSI codes are printed properly`() {

        fun test(message: String) {
            logger.info { "${message}${Ansi.RESET} | normal text" }
        }

        test("${Ansi.BLACK_FG}black fg")
        test("${Ansi.RED_FG}red fg")
        test("${Ansi.GREEN_FG}green fg")
        test("${Ansi.YELLOW_FG}yellow fg")
        test("${Ansi.BLUE_FG}blue fg")
        test("${Ansi.MAGENTA_FG}magenta fg")
        test("${Ansi.CYAN_FG}cyan fg")
        test("${Ansi.WHITE_FG}white fg")
        test("${Ansi.DEFAULT_FG}default fg")

        test("${Ansi.BLACK_BG}black bg")
        test("${Ansi.RED_BG}red bg")
        test("${Ansi.GREEN_BG}green bg")
        test("${Ansi.YELLOW_BG}yellow bg")
        test("${Ansi.BLUE_BG}blue bg")
        test("${Ansi.MAGENTA_BG}magenta bg")
        test("${Ansi.CYAN_BG}cyan bg")
        test("${Ansi.WHITE_BG}white bg")
        test("${Ansi.DEFAULT_BG}default bg")

        test("${Ansi.BOLD_ON}bold on")
        test("${Ansi.UNDERLINE_ON}underline on")
        test("${Ansi.BLINK_ON}blink on")
        test("${Ansi.INVERSE_ON}inverse on")

        test("${Ansi.BOLD_OFF}bold off")
        test("${Ansi.UNDERLINE_OFF}underline off")
        test("${Ansi.BLINK_OFF}blink off")
        test("${Ansi.INVERSE_OFF}inverse off")

        test("${Ansi.BLACK_FG}${Ansi.BLUE_BG}${Ansi.UNDERLINE_ON}black fg blue bg underline")
    }
}
