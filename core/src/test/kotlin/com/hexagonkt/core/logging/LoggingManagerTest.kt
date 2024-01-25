package com.hexagonkt.core.logging

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.condition.DisabledInNativeImage
import kotlin.IllegalStateException
import kotlin.reflect.KClass
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class LoggingManagerTest {

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
    @DisabledInNativeImage
    fun `Problem reading class name raises error`() {
        val kc = mockk<KClass<*>>()
        every { kc.qualifiedName } returns null

        assertFailsWith<IllegalStateException> { Logger(kc) }
            .apply { assertEquals("Cannot get qualified name of type", message) }
    }
}
