package com.hexagonkt.core.logging

import com.hexagonkt.core.logging.LoggingLevel.*
import com.hexagonkt.core.logging.jul.JulLoggingAdapter
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LoggingManagerTest {

    // TODO Repeat this test on other logging adapters
    @Test fun `Loggers are enabled and disabled in runtime`() {

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
}
