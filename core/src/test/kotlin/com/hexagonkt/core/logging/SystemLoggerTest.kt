package com.hexagonkt.core.logging

import com.hexagonkt.core.logging.LoggingLevel.*
import org.junit.jupiter.api.Test
import java.lang.RuntimeException
import kotlin.test.assertFailsWith

// TODO Add string appender and assert messages
internal class SystemLoggerTest {

    @Test fun `System loggers work as expected`() {
        LoggingManager.adapter = SystemLoggingAdapter(TRACE)
        val l = Logger("a")

        l.log(TRACE) { "trace" }
        l.log(DEBUG) { "debug" }
        l.log(INFO) { "info" }
        l.log(WARN) { "warn" }
        l.log(ERROR) { "error" }

        l.log(TRACE, RuntimeException()) { "trace $it" }
        l.log(DEBUG, RuntimeException()) { "debug $it" }
        l.log(INFO, RuntimeException()) { "info $it" }
        l.log(WARN, RuntimeException()) { "warn $it" }
        l.log(ERROR, RuntimeException()) { "error $it" }

        LoggingManager.adapter = SystemLoggingAdapter()
    }

    @Test fun `Disabled logs are not issued`() {
        LoggingManager.adapter = SystemLoggingAdapter(DEBUG)
        val l = Logger("a")

        l.log(TRACE) { "trace" }
        l.log(TRACE, RuntimeException()) { "trace $it" }

        LoggingManager.adapter = SystemLoggingAdapter()
    }

    @Test fun `Invalid log level throws an error`() {
        assertFailsWith<IllegalStateException> { logger.log(OFF) { "error" } }
        assertFailsWith<IllegalStateException> {
            logger.log(OFF, RuntimeException()) { "error $it" }
        }
    }
}
