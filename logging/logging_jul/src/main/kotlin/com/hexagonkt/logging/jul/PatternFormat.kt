package com.hexagonkt.logging.jul

import com.hexagonkt.core.text.AnsiColor.DEFAULT
import com.hexagonkt.core.text.AnsiColor.YELLOW
import com.hexagonkt.core.text.AnsiColor.BLUE
import com.hexagonkt.core.text.AnsiColor.BRIGHT_BLACK
import com.hexagonkt.core.text.AnsiColor.CYAN
import com.hexagonkt.core.text.AnsiColor.MAGENTA
import com.hexagonkt.core.text.AnsiColor.RED
import com.hexagonkt.core.text.AnsiEffect.BOLD
import com.hexagonkt.core.text.Ansi.RESET
import com.hexagonkt.core.text.eol
import com.hexagonkt.core.fail
import com.hexagonkt.core.toText
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord

/**
 * A Formatter implements [Formatter] provides support for formatting Logs.
 *
 * @property useColor Use colors in log messages.
 */
class PatternFormat(
    private val useColor: Boolean,
    private val messageOnly: Boolean = false,
) : Formatter() {

    internal companion object {
        private const val TIMESTAMP = "%tH:%<tM:%<tS,%<tL"
        private const val LEVEL = "%-5s"
        private const val THREAD = "[%-15s]"
        private const val LOGGER = "%-30s"

        const val PATTERN = "$TIMESTAMP $LEVEL $THREAD $LOGGER | %s%n"
        const val COLOR_PATTERN =
            "$BRIGHT_BLACK$TIMESTAMP %s$LEVEL$RESET $MAGENTA$THREAD $CYAN$LOGGER$RESET | %s%n$RESET"
    }

    private val pattern: String = if (useColor) COLOR_PATTERN else PATTERN

    private val levelColors: Map<Level, String> = mapOf(
        Level.FINER to DEFAULT,
        Level.FINE to DEFAULT,
        Level.INFO to BLUE,
        Level.WARNING to YELLOW,
        Level.SEVERE to RED + BOLD
    )

    private val levelNames: Map<Level, String> = mapOf(
        Level.FINER to "TRACE",
        Level.FINE to "DEBUG",
        Level.INFO to "INFO",
        Level.WARNING to "WARN",
        Level.SEVERE to "ERROR"
    )

    override fun format(record: LogRecord): String {
        if (messageOnly)
            return record.message + "\n"

        val instant = Instant.ofEpochMilli(record.millis)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val thrown = record.thrown
        val trace = when {
            thrown == null -> ""
            useColor -> "$eol${thrown.toText()}".replace("\n", "\n$RED")
            else -> "$eol${thrown.toText()}"
        }
        val level = record.level
        val levelName = levelNames[level] ?: fail
        val levelColor = levelColors[level] ?: BLUE
        val message = record.message
        val loggerName = record.loggerName
        val threadName = Thread.currentThread().name

        return if (useColor)
            pattern.format(dateTime, levelColor, levelName, threadName, loggerName, message + trace)
        else
            pattern.format(dateTime, levelName, threadName, loggerName, message + trace)
    }
}
