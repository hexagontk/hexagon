package com.hexagonkt.core.logging

import com.hexagonkt.core.logging.LoggingLevel.*
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import kotlin.IllegalStateException
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LoggingManagerTest {

    // TODO Repeat this test on other logging adapters
    @Test fun `Loggers are enabled and disabled at runtime`() {

        LoggingManager.adapter = PrintLoggingAdapter()
        val allLevels = LoggingLevel.values()

        val ch = Logger("com.hx")
        val chc = Logger("com.hx.core")
        val chl = Logger("com.hx.logging")

        LoggingManager.setLoggerLevel("com.hx", TRACE)
        assertTrue(Logger("z").isLoggerLevelEnabled(INFO))
        assertFalse(Logger("z").isLoggerLevelEnabled(DEBUG))
        assertTrue(
            allLevels.all {
                ch.isLoggerLevelEnabled(it)
                    && chc.isLoggerLevelEnabled(it)
                    && chl.isLoggerLevelEnabled(it)
            }
        )

        LoggingManager.setLoggerLevel("com.hx.core", WARN)
        assertTrue(chc.isLoggerLevelEnabled(ERROR) && chc.isErrorEnabled())
        assertTrue(chc.isLoggerLevelEnabled(WARN) && chc.isWarnEnabled())
        assertFalse(chc.isLoggerLevelEnabled(INFO) || chc.isInfoEnabled())
        assertFalse(chc.isLoggerLevelEnabled(DEBUG) || chc.isDebugEnabled())
        assertFalse(chc.isLoggerLevelEnabled(TRACE) || chc.isTraceEnabled())
        assertTrue(allLevels.all { ch.isLoggerLevelEnabled(it) && chl.isLoggerLevelEnabled(it) })

        // TODO Check if parent level changes gets reflected on created loggers (com.hx -> TRACE)
        LoggingManager.setLoggerLevel("com.hx.core", TRACE)
        assertTrue(LoggingManager.isLoggerLevelEnabled("com.hx.core", INFO))
        assertTrue(chc.isLoggerLevelEnabled(ERROR) && chc.isErrorEnabled())
        assertTrue(chc.isLoggerLevelEnabled(WARN) && chc.isWarnEnabled())
        assertTrue(chc.isLoggerLevelEnabled(INFO) && chc.isInfoEnabled())
        assertTrue(chc.isLoggerLevelEnabled(DEBUG) && chc.isDebugEnabled())
        assertTrue(chc.isLoggerLevelEnabled(TRACE) && chc.isTraceEnabled())
    }

    @Test fun `'defaultLoggerName' can be changed`() {
        val dln = LoggingManager.defaultLoggerName

        LoggingManager.defaultLoggerName = "com.example"
        assertEquals("com.example", LoggingManager.defaultLoggerName)

        LoggingManager.defaultLoggerName = dln
    }

    @Test fun `'defaultLoggerName' cannot be set to empty string`() {
        val e = assertFailsWith<IllegalArgumentException> {
            LoggingManager.defaultLoggerName = ""
        }

        assertEquals("Default logger name cannot be empty string", e.message)
    }

    @Test
    @DisabledIfSystemProperty(named = "nativeTest", matches = "true")
    fun `Problem reading class name raises error`() {
        val kc = mockk<KClass<*>>()
        every { kc.qualifiedName } returns null

        assertFailsWith<IllegalStateException> { LoggingManager.isLoggerLevelEnabled(kc, INFO) }
            .apply { assertEquals("Cannot get qualified name of type", message) }

        assertFailsWith<IllegalStateException> { LoggingManager.setLoggerLevel(kc, INFO) }
            .apply { assertEquals("Cannot get qualified name of type", message) }
    }
}
