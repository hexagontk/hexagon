package com.hexagonkt.core.logging

import com.hexagonkt.core.logging.LoggingLevel.*
import com.hexagonkt.core.logging.jul.JulLoggingAdapter
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LoggingManagerTest {

    // TODO Repeat this test on other logging adapters
    @Test fun `Loggers are enabled and disabled at runtime`() {

        LoggingManager.adapter = JulLoggingAdapter()
        val allLevels = LoggingLevel.values()

        val ch = Logger("com.hx")
        val chc = Logger("com.hx.core")
        val chl = Logger("com.hx.logging")

        LoggingManager.setLoggerLevel("com.hx", TRACE)
        assertTrue(
            allLevels.all {
                ch.isLoggerLevelEnabled(it)
                    && chc.isLoggerLevelEnabled(it)
                    && chl.isLoggerLevelEnabled(it)
            }
        )

        LoggingManager.setLoggerLevel("com.hx.core", WARN)
        assertTrue(chc.isLoggerLevelEnabled(ERROR))
        assertTrue(chc.isLoggerLevelEnabled(WARN))
        assertFalse(chc.isLoggerLevelEnabled(INFO))
        assertFalse(chc.isLoggerLevelEnabled(DEBUG))
        assertFalse(chc.isLoggerLevelEnabled(TRACE))
        assertTrue(allLevels.all { ch.isLoggerLevelEnabled(it) && chl.isLoggerLevelEnabled(it) })

        // TODO Check if parent level changes gets reflected on created loggers (com.hx -> TRACE)
        LoggingManager.setLoggerLevel("com.hx.core", TRACE)
        assertTrue(LoggingManager.isLoggerLevelEnabled("com.hx.core", INFO))
        assertTrue(chc.isLoggerLevelEnabled(ERROR))
        assertTrue(chc.isLoggerLevelEnabled(WARN))
        assertTrue(chc.isLoggerLevelEnabled(INFO))
        assertTrue(chc.isLoggerLevelEnabled(DEBUG))
        assertTrue(chc.isLoggerLevelEnabled(TRACE))
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

    @Test fun `Problem reading class name raises error`() {
        val kc = mockk<KClass<*>>()
        every { kc.qualifiedName } returns null

        assertFailsWith<IllegalStateException> { LoggingManager.isLoggerLevelEnabled(kc, INFO) }
            .apply { assertEquals("Cannot get qualified name of type", message) }

        assertFailsWith<IllegalStateException> { LoggingManager.setLoggerLevel(kc, INFO) }
            .apply { assertEquals("Cannot get qualified name of type", message) }
    }
}
