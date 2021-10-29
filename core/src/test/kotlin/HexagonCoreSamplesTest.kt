package com.hexagonkt.core

import com.hexagonkt.core.helpers.Jvm
import com.hexagonkt.core.logging.logger
import com.hexagonkt.core.logging.Logger
import com.hexagonkt.serialization.json.Json
import com.hexagonkt.core.serialization.SerializationManager
import com.hexagonkt.core.serialization.parse
import com.hexagonkt.core.serialization.serialize
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class HexagonCoreSamplesTest {

    internal data class Person(
        val givenName: String,
        val familyName: String,
        val birthDate: LocalDate
    )

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
        classLogger.error { "Error without an exception" }

        classLogger.time("Logs the time used to run the following block of code") {
            val message = "Block of code to be timed"
            assert(message.isNotBlank())
        }

        instanceLogger.flare { "Prints a log that stands out for ease searching" }
        // logger
    }

    @Test fun serializationUsage() {
        // serializationUsage
        SerializationManager.formats = linkedSetOf(Json) // Loads JSON format (using it as default)
        val jason = Person("Jason", "Jackson", LocalDate.of(1989, 12, 31))

        val jasonJson = jason.serialize(Json) // Can also be Yaml or an string: "application/json"
        val parsedJason = jasonJson.parse(Person::class) // Uses default format (JSON)

        assert(jason == parsedJason)
        assert(jason !== parsedJason)
        // serializationUsage
    }
}
