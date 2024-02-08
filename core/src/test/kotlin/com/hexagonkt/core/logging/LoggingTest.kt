package com.hexagonkt.core.logging

import com.hexagonkt.core.urlOf
import org.junit.jupiter.api.Test
import java.util.logging.LogManager
import kotlin.test.assertEquals

internal class LoggingTest {

    @Test fun `Log helpers`() {
        val configuration = urlOf("classpath:sample.properties")
        LogManager.getLogManager().readConfiguration(configuration.openStream())

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
