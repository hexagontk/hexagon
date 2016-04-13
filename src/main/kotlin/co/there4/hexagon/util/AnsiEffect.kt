package co.there4.hexagon.util

/**
 * TODO .
 *
 * @author jam
 */
enum class AnsiEffect (code: Int, on: Boolean) {
    BOLD (1, true),
    UNDERLINE (4, true),
    BLINK (5, true),
    INVERSE (7, true),

    BOLD_OFF (1, false),
    UNDERLINE_OFF (4, false),
    BLINK_OFF (5, false),
    INVERSE_OFF (7, false);

    val code = if (on) code else code + SWITCH_EFFECT
}
