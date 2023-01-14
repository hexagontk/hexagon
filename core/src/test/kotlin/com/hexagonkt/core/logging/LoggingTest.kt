package com.hexagonkt.core.logging

import com.hexagonkt.core.logging.LoggingLevel.*
import kotlin.test.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LoggingTest {

    @Test fun `Log helpers`() {
        LoggingManager.useColor = false
        assertEquals(LoggingManager.defaultLoggerName, logger.name)

        assertEquals("foo", "foo".trace(">>> "))
        assertEquals("foo", "foo".trace())
        assertEquals(null, null.trace())
        assertEquals("text", "text".trace())

        assertEquals("foo", "foo".debug(">>> "))
        assertEquals("foo", "foo".debug())
        assertEquals(null, null.debug())
        assertEquals("text", "text".debug())

        assertEquals("foo", "foo".info(">>> "))
        assertEquals("foo", "foo".info())
        assertEquals(null, null.info())
        assertEquals("text", "text".info())

        LoggingManager.useColor = true

        assertEquals("foo", "foo".trace(">>> "))
        assertEquals("foo", "foo".trace())
        assertEquals(null, null.trace())
        assertEquals("text", "text".trace())

        assertEquals("foo", "foo".debug(">>> "))
        assertEquals("foo", "foo".debug())
        assertEquals(null, null.debug())
        assertEquals("text", "text".debug())

        assertEquals("foo", "foo".info(">>> "))
        assertEquals("foo", "foo".info())
        assertEquals(null, null.info())
        assertEquals("text", "text".info())
    }

    @Test fun `Manager can check if the root logger is enabled for a given level`() {
        LoggingManager.setLoggerLevel(TRACE)
        assertTrue(LoggingManager.isLoggerLevelEnabled(TRACE))
        assertTrue(LoggingManager.isLoggerLevelEnabled(DEBUG))
        assertTrue(LoggingManager.isLoggerLevelEnabled(INFO))
        assertTrue(LoggingManager.isLoggerLevelEnabled(WARN))
        assertTrue(LoggingManager.isLoggerLevelEnabled(ERROR))

        LoggingManager.setLoggerLevel(DEBUG)
        assertFalse(LoggingManager.isLoggerLevelEnabled(TRACE))
        assertTrue(LoggingManager.isLoggerLevelEnabled(DEBUG))
        assertTrue(LoggingManager.isLoggerLevelEnabled(INFO))
        assertTrue(LoggingManager.isLoggerLevelEnabled(WARN))
        assertTrue(LoggingManager.isLoggerLevelEnabled(ERROR))

        LoggingManager.setLoggerLevel(INFO)
        assertFalse(LoggingManager.isLoggerLevelEnabled(TRACE))
        assertFalse(LoggingManager.isLoggerLevelEnabled(DEBUG))
        assertTrue(LoggingManager.isLoggerLevelEnabled(INFO))
        assertTrue(LoggingManager.isLoggerLevelEnabled(WARN))
        assertTrue(LoggingManager.isLoggerLevelEnabled(ERROR))

        LoggingManager.setLoggerLevel(WARN)
        assertFalse(LoggingManager.isLoggerLevelEnabled(TRACE))
        assertFalse(LoggingManager.isLoggerLevelEnabled(DEBUG))
        assertFalse(LoggingManager.isLoggerLevelEnabled(INFO))
        assertTrue(LoggingManager.isLoggerLevelEnabled(WARN))
        assertTrue(LoggingManager.isLoggerLevelEnabled(ERROR))

        LoggingManager.setLoggerLevel(ERROR)
        assertFalse(LoggingManager.isLoggerLevelEnabled(TRACE))
        assertFalse(LoggingManager.isLoggerLevelEnabled(DEBUG))
        assertFalse(LoggingManager.isLoggerLevelEnabled(INFO))
        assertFalse(LoggingManager.isLoggerLevelEnabled(WARN))
        assertTrue(LoggingManager.isLoggerLevelEnabled(ERROR))

        LoggingManager.setLoggerLevel(OFF)
        assertFalse(LoggingManager.isLoggerLevelEnabled(TRACE))
        assertFalse(LoggingManager.isLoggerLevelEnabled(DEBUG))
        assertFalse(LoggingManager.isLoggerLevelEnabled(INFO))
        assertFalse(LoggingManager.isLoggerLevelEnabled(WARN))
        assertFalse(LoggingManager.isLoggerLevelEnabled(ERROR))

        LoggingManager.setLoggerLevel(WARN)
    }

    @Test fun `Manager can check if loggers are enabled for a given level`() {
        val date = LocalDate.now()

        LoggingManager.setLoggerLevel(OFF)

        LoggingManager.setLoggerLevel(date, TRACE)
        assertTrue(LoggingManager.isLoggerLevelEnabled(LocalDate::class, TRACE))
        assertTrue(LoggingManager.isLoggerLevelEnabled(date, TRACE))
        assertTrue(LoggingManager.isLoggerLevelEnabled(date, DEBUG))
        assertTrue(LoggingManager.isLoggerLevelEnabled(date, INFO))
        assertTrue(LoggingManager.isLoggerLevelEnabled(date, WARN))
        assertTrue(LoggingManager.isLoggerLevelEnabled(date, ERROR))

        LoggingManager.setLoggerLevel(date, DEBUG)
        assertTrue(LoggingManager.isLoggerLevelEnabled(LocalDate::class, DEBUG))
        assertFalse(LoggingManager.isLoggerLevelEnabled(date, TRACE))
        assertTrue(LoggingManager.isLoggerLevelEnabled(date, DEBUG))
        assertTrue(LoggingManager.isLoggerLevelEnabled(date, INFO))
        assertTrue(LoggingManager.isLoggerLevelEnabled(date, WARN))
        assertTrue(LoggingManager.isLoggerLevelEnabled(date, ERROR))

        LoggingManager.setLoggerLevel(date, INFO)
        assertTrue(LoggingManager.isLoggerLevelEnabled(LocalDate::class, INFO))
        assertFalse(LoggingManager.isLoggerLevelEnabled(date, TRACE))
        assertFalse(LoggingManager.isLoggerLevelEnabled(date, DEBUG))
        assertTrue(LoggingManager.isLoggerLevelEnabled(date, INFO))
        assertTrue(LoggingManager.isLoggerLevelEnabled(date, WARN))
        assertTrue(LoggingManager.isLoggerLevelEnabled(date, ERROR))

        LoggingManager.setLoggerLevel(date, WARN)
        assertTrue(LoggingManager.isLoggerLevelEnabled(LocalDate::class, WARN))
        assertFalse(LoggingManager.isLoggerLevelEnabled(date, TRACE))
        assertFalse(LoggingManager.isLoggerLevelEnabled(date, DEBUG))
        assertFalse(LoggingManager.isLoggerLevelEnabled(date, INFO))
        assertTrue(LoggingManager.isLoggerLevelEnabled(date, WARN))
        assertTrue(LoggingManager.isLoggerLevelEnabled(date, ERROR))

        LoggingManager.setLoggerLevel(date, ERROR)
        assertTrue(LoggingManager.isLoggerLevelEnabled(LocalDate::class, ERROR))
        assertFalse(LoggingManager.isLoggerLevelEnabled(date, TRACE))
        assertFalse(LoggingManager.isLoggerLevelEnabled(date, DEBUG))
        assertFalse(LoggingManager.isLoggerLevelEnabled(date, INFO))
        assertFalse(LoggingManager.isLoggerLevelEnabled(date, WARN))
        assertTrue(LoggingManager.isLoggerLevelEnabled(date, ERROR))

        LoggingManager.setLoggerLevel(date, OFF)
        assertFalse(LoggingManager.isLoggerLevelEnabled(LocalDate::class, ERROR))
        assertFalse(LoggingManager.isLoggerLevelEnabled(date, TRACE))
        assertFalse(LoggingManager.isLoggerLevelEnabled(date, DEBUG))
        assertFalse(LoggingManager.isLoggerLevelEnabled(date, INFO))
        assertFalse(LoggingManager.isLoggerLevelEnabled(date, WARN))
        assertFalse(LoggingManager.isLoggerLevelEnabled(date, ERROR))

        LoggingManager.setLoggerLevel(WARN)
    }
}
