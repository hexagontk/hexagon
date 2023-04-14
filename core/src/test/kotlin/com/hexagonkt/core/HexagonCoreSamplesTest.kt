package com.hexagonkt.core

import com.hexagonkt.core.logging.logger
import com.hexagonkt.core.logging.Logger
import com.hexagonkt.core.logging.LoggingLevel.*
import com.hexagonkt.core.logging.LoggingManager
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertContains

internal class HexagonCoreSamplesTest {

    @Test fun buildPropertiesBundled() {
        val hexagonProperties = URL("classpath:hexagon.properties").readText()

        assertContains(hexagonProperties, "project=")
        assertContains(hexagonProperties, "module=")
        assertContains(hexagonProperties, "version=")
        assertContains(hexagonProperties, "group=")
        assertContains(hexagonProperties, "description=")
    }

    @Suppress("RedundantExplicitType") // Type declared for examples generation
    @Test fun loggerUsage() {
        // logger
        val classLogger: Logger = Logger(Runtime::class) // Logger for the `Runtime` class
        val instanceLogger: Logger = Logger(this::class) // Logger for this instance's class

        logger.info {
            """
            You can add a quick log without declaring a Logger with 'com.hexagonkt.helpers.logger'.
            It is a default logger created for the System class (same as `Logger(System::class)`).
            """
        }

        classLogger.trace { "Message only evaluated if trace enabled at ${Jvm.id}" }
        classLogger.debug { "Message only evaluated if debug enabled at ${Jvm.id}" }
        classLogger.warn { "Message only evaluated if warn enabled at ${Jvm.id}" }
        classLogger.info { "Message only evaluated if info enabled at ${Jvm.id}" }

        val exception = IllegalStateException("Exception")
        classLogger.warn(exception) { "Warning with exception" }
        classLogger.error(exception) { "Error message with exception" }
        classLogger.warn(exception)
        classLogger.error(exception)
        classLogger.error { "Error without an exception" }

        instanceLogger.flare { "Prints a log that stands out for ease searching" }

        // Logging level can be changed programmatically
        LoggingManager.setLoggerLevel(ERROR)
        LoggingManager.setLoggerLevel(classLogger::class, DEBUG)
        LoggingManager.setLoggerLevel("com.hexagonkt", INFO)
        // logger
    }
}
