package com.hexagonkt.core.logging

import com.hexagonkt.core.urlOf
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.util.logging.LogManager
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
internal class LoggingTest {

    @BeforeAll fun setUp() {
        val configuration = urlOf("classpath:sample.properties")
        LogManager.getLogManager().readConfiguration(configuration.openStream())
    }

    @Test fun `Log helpers`() {
        assertEquals(defaultLoggerName, logger.name)

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
}
