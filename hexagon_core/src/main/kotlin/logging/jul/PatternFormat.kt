package com.hexagonkt.logging.jul

import com.hexagonkt.helpers.Ansi
import com.hexagonkt.helpers.Ansi.BLUE
import com.hexagonkt.helpers.Ansi.BRIGHT_BLACK
import com.hexagonkt.helpers.Ansi.CYAN
import com.hexagonkt.helpers.Ansi.MAGENTA
import com.hexagonkt.helpers.Ansi.RED
import com.hexagonkt.helpers.Ansi.RESET
import com.hexagonkt.helpers.eol
import com.hexagonkt.helpers.fail
import com.hexagonkt.helpers.toText
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord

class PatternFormat(private val useColor: Boolean = true) : Formatter() {

    private companion object {
        const val TIMESTAMP = "%tH:%<tM:%<tS,%<tL"
        const val LEVEL = "%-5s"
        const val THREAD = "[%-15s]"
        const val LOGGER = "%-30s"
        const val PATTERN = "$TIMESTAMP $LEVEL $THREAD $LOGGER | %s%n"
        const val COLOR_PATTERN =
            "$BRIGHT_BLACK$TIMESTAMP %s$LEVEL$RESET $MAGENTA$THREAD $CYAN$LOGGER$RESET | %s%n"
    }

    private val pattern: String = if (useColor) COLOR_PATTERN else PATTERN

    private val levelColors: Map<Level, String> = mapOf(
        Level.FINER to Ansi.DEFAULT,
        Level.FINE to Ansi.DEFAULT,
        Level.INFO to BLUE,
        Level.WARNING to Ansi.YELLOW,
        Level.SEVERE to RED + Ansi.BOLD
    )

    private val levelNames: Map<Level, String> = mapOf(
        Level.FINER to "TRACE",
        Level.FINE to "DEBUG",
        Level.INFO to "INFO",
        Level.WARNING to "WARN",
        Level.SEVERE to "ERROR"
    )

    override fun format(record: LogRecord): String {
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
        val parameters = record.parameters ?: emptyArray()
        val loggerName = record.loggerName
        val threadName = Thread.currentThread().name
        val msg = String.format(message, *parameters) + trace

        return if (useColor)
            pattern.format(dateTime, levelColor, levelName, threadName, loggerName, msg)
        else
            pattern.format(dateTime, levelName, threadName, loggerName, msg)
    }
}
