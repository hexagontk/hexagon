package co.there4.hexagon.util

import java.lang.ClassLoader.getSystemClassLoader
import java.lang.System.getProperty

/** Runtime specific end of line. */
val EOL = getProperty("line.separator")

/** Variable prefix for string filtering. It starts with '#' because of Kotlin's syntax. */
private val VARIABLE_PREFIX = "#{"
/** Variable sufix for string filtering. */
private val VARIABLE_SUFFIX = "}"
/** Delimiter char for banners. */
private val BANNER_DELIMITER = "*"

/** Start of ANSI sequence. */
private val ANSI_PREFIX = "\u001B["
/** End of ANSI sequence. */
private val ANSI_END = "m"

/** Separator for commands inside a single ANSI sequence. */
private val ANSI_SEPARATOR = ";"
/** ANSI command to reset all attributes. */
private val ANSI_RESET = "0"

/** ANSI foreground color base. */
val FOREGROUND = 30
/** ANSI background color base. */
val BACKGROUND = 40

/** ANSI modifier to switch and effect (add to enable substract todisable). */
val SWITCH_EFFECT = 20

/**
 * Filters the target string substituting each key by its value. The keys format is:
 * `#{key}` and all occurrences are replaced by the supplied value.
 *
 * If a variable does not have a parameter, it is left as it is.
 *
 * @param parameters The map with the list of key/value tuples.
 * @return The filtered text or the same string if no values are passed or found in the text.
 */
fun String.filterVars(parameters: Map<*, *>) =
    parameters.entries
        .filter { it.key.toString().isNotEmpty() }
        .fold(this) { result, pair ->
            val key = pair.key.toString()
            val value = pair.value.toString()
            result.replace ("${VARIABLE_PREFIX}$key${VARIABLE_SUFFIX}", value)
        }

fun String.filterVars(vararg parameters: Pair<*, *>) = this.filterVars (mapOf (*parameters))

fun String.filter(prefix: String, suffix: String, vararg parameters: Pair<String, String>) =
    parameters.fold(this, { result, pair -> result.replace (prefix + pair.first + suffix, pair.second) })

fun String.filter(vararg parameters: Pair<String, String>) = this.filter("", "", *parameters)

fun Regex.findGroups (str: String): List<MatchGroup> =
    (this.find (str)?.groups ?: listOf<MatchGroup> ())
        .map { it ?: throw IllegalArgumentException () }
        .drop(1)

/**
 * Transforms the target string from snake case to camel case.
 */
fun String.snakeToCamel () =
    this.split ("_")
        .filter { it.isNotEmpty () }
        .map { it.capitalize () }
        .joinToString("")
        .decapitalize ()

/**
 * Transforms the target string from camel case to snake case.
 */
fun String.camelToSnake () =
    this.split ("(?=\\p{Upper}\\p{Lower})".toRegex())
        .filter { it.isNotEmpty () }
        .map { it.toLowerCase () }
        .joinToString ("_")
        .decapitalize ()

/**
 * Formats the string as a banner with a delimiter above and below text. The character used to
 * render the delimiter is defined [here][BANNER_DELIMITER]
 */
fun String.banner (): String {
    val separator = BANNER_DELIMITER.repeat (this.lines().map { it.length }.max() ?: 0)
    return "$separator${EOL}$this${EOL}$separator"
}

fun read (resource: String) =
    getSystemClassLoader().getResourceAsStream(resource)?.reader()?.readText()

private fun ansiCode(fg: AnsiColor?, bg: AnsiColor?, vararg fxs: AnsiEffect): String {
    fun fgString (color: AnsiColor?) = (color?.fg ?: "").toString()
    fun bgString (color: AnsiColor?) = (color?.bg ?: "").toString()

    val colors = listOf (fgString(fg), bgString(bg))
    val elements = colors + fxs.map { it.code.toString() }
    val body = elements.filter { it.isNotEmpty() }.joinToString (ANSI_SEPARATOR)

    return ANSI_PREFIX + (if (body.isEmpty()) ANSI_RESET else body) + ANSI_END
}

/**
 * Creates an ANSI sequence composed by a list of commands (colors, effects, etc.).
 *
 * A call with no effect neither color generates an ANSI reset.
 *
 * @param fg Foreground color.
 * @param bg Background color.
 * @param fxs List of affects
 * @return The ANSI sequence
 */
fun ansi(fg: AnsiColor, bg: AnsiColor, vararg fxs: AnsiEffect) = ansiCode (fg, bg, *fxs)
fun ansi(fg: AnsiColor, vararg fxs: AnsiEffect) = ansiCode (fg, null, *fxs)
fun ansi(vararg fxs: AnsiEffect) = ansiCode (null, null, *fxs)

