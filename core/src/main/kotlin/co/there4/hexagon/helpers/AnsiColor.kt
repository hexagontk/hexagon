package co.there4.hexagon.helpers

enum class AnsiColor (
    code: Int,
    val fg: Int = FOREGROUND + code,
    val bg: Int = BACKGROUND + code) {

    BLACK (0),
    RED (1),
    GREEN (2),
    YELLOW (3),
    BLUE (4),
    MAGENTA (5),
    CYAN (6),
    WHITE (7),

    DEFAULT (9)
}
