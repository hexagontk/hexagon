package com.hexagonkt.core.logging

import com.hexagonkt.core.logging.LoggingLevel.ERROR
import com.hexagonkt.core.logging.LoggingLevel.TRACE
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class LoggerTest {

    /**
     * As the logger is only a facade, and it is hard to check outputs, the only check is that no
     * exceptions are thrown.
     */
    @Test fun `Messages are logged without errors using JUL`() {

        val logger = Logger(this::class)

        logger.trace { 42 }
        logger.debug { true }
        logger.info { 0.0 }
        logger.warn { listOf(0, 1) }
        logger.error { mapOf(0 to 1, 2 to 3) }
        logger.warn(RuntimeException()) { 'c' }
        logger.error(RuntimeException()) { 0..100 }
        logger.warn(null) { assertNull(it) }
        logger.error(null) { assertNull(it) }
        logger.flare { "message" }
        logger.time("message") {}
        logger.time {}
        logger.log(TRACE) { "message" }
        logger.log(ERROR, RuntimeException()) { 0..100 }
    }

    @Test fun `A logger for a custom name has the proper name`() {
        assert(Logger("name").name == "name")
        assert(Logger("name"::class).name == "kotlin.String")
    }

    @Test fun `Invalid class name raises error`() {
        val kc = mockk<KClass<*>>()
        every { kc.qualifiedName } returns null
        val e = assertFailsWith<IllegalStateException> { Logger(kc) }
        assertEquals("Cannot get qualified name of type", e.message)
    }
}
