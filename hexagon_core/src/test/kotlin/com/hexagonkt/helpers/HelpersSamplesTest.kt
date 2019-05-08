package com.hexagonkt.helpers

import org.testng.annotations.Test
import java.lang.IllegalStateException

class HelpersSamplesTest {

    @Test fun loggerUsage() {
        // logger
        val classLogger: Logger = Logger(Runtime::class) // Logger for the `Runtime` class
        val instanceLogger: Logger = Logger(this) // Logger for this instance's class

        classLogger.trace { "Message only evaluated if trace enabled at ${Jvm.id}" }
        classLogger.debug { "Message only evaluated if debug enabled at ${Jvm.id}" }
        classLogger.warn { "Message only evaluated if warn enabled at ${Jvm.id}" }
        classLogger.info { "Message only evaluated if info enabled at ${Jvm.id}" }

        val exception = IllegalStateException("Exception")
        classLogger.warn(exception) { "Warning with exception" }
        classLogger.error(exception) { "Error message with exception" }
        classLogger.error { "Error without an exception" }

        classLogger.time("Logs the time used to run the following block of code") {
            val message = "Block of code to be timed"
            assert(message.isNotBlank())
        }

        instanceLogger.flare { "Prints a log that stands out for ease searching" }
        // logger
    }
}
