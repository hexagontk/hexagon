package com.hexagontk.jul

import com.hexagontk.core.fail
import com.hexagontk.core.text.AnsiColor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.lang.RuntimeException
import java.util.logging.Level.INFO
import java.util.logging.Level.SEVERE
import java.util.logging.LogRecord

internal class PatternFormatTest {

    @Test fun `Formatting messages with 'printf' special characters works correctly`() {
        val message = "Message with '%'"

        val colorFormat = PatternFormat(true)
        val colorMessage = colorFormat.format(LogRecord(INFO, message))
        Assertions.assertTrue(colorMessage.contains(message))
        Assertions.assertTrue(colorMessage.contains(AnsiColor.BLUE))

        val plainFormat = PatternFormat(false)
        val plainMessage = plainFormat.format(LogRecord(INFO, message))
        Assertions.assertTrue(plainMessage.contains(message))
        Assertions.assertFalse(plainMessage.contains(AnsiColor.BLUE))
    }

    @Test fun `Formatting error messages render stack traces`() {
        val message = "Message with '%'"
        val record = LogRecord(SEVERE, message)
        record.thrown = RuntimeException("Tested failure")

        val colorMessage = PatternFormat(true).format(record)
        Assertions.assertTrue(colorMessage.contains(message))
        Assertions.assertTrue(colorMessage.contains(AnsiColor.RED))
        Assertions.assertTrue(colorMessage.contains("Tested failure"))
        Assertions.assertTrue(colorMessage.contains(RuntimeException::class.qualifiedName ?: fail))
        Assertions.assertTrue(colorMessage.contains(this::class.qualifiedName ?: fail))

        val plainMessage = PatternFormat(false).format(record)
        Assertions.assertTrue(plainMessage.contains(message))
        Assertions.assertFalse(plainMessage.contains(AnsiColor.RED))
        Assertions.assertTrue(plainMessage.contains("Tested failure"))
        Assertions.assertTrue(plainMessage.contains(RuntimeException::class.qualifiedName ?: fail))
        Assertions.assertTrue(plainMessage.contains(this::class.qualifiedName ?: fail))
    }

    @Test fun `Formatting messages without logging fields works correctly`() {
        val message = "Message with '%'"

        val colorFormat = PatternFormat(useColor = true, messageOnly = true)
        val colorMessage = colorFormat.format(LogRecord(INFO, message))
        Assertions.assertTrue(colorMessage.contains(message))
        Assertions.assertFalse(colorMessage.contains("INFO"))
        Assertions.assertFalse(colorMessage.contains(AnsiColor.BLUE))

        val plainFormat = PatternFormat(useColor = false, messageOnly = true)
        val plainMessage = plainFormat.format(LogRecord(INFO, message))
        Assertions.assertTrue(plainMessage.contains(message))
        Assertions.assertFalse(colorMessage.contains("INFO"))
        Assertions.assertFalse(plainMessage.contains(AnsiColor.BLUE))
    }
}
