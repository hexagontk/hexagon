package com.hexagonkt

import com.hexagonkt.helpers.Jvm
import com.hexagonkt.logging.logger
import com.hexagonkt.injection.InjectionManager.module
import com.hexagonkt.injection.InjectionManager.injector
import com.hexagonkt.injection.forceBind
import com.hexagonkt.logging.Logger
import com.hexagonkt.serialization.Json
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.parse
import com.hexagonkt.serialization.serialize
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertSame

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

    @Suppress("RemoveExplicitTypeArguments")
    @Test fun injectionUsage() {
        module.clear()

        // injectionUsage
        // This snippet assumes the following IMPORTS ARE DECLARED:
        // import com.hexagonkt.injection.InjectionManager.module
        // import com.hexagonkt.injection.InjectionManager.injector

        // Bind classes to functions (create a different instance with each `inject` call)
        module.bind<Date> { java.sql.Date(System.currentTimeMillis()) }

        // Bind classes to objects (returns the same instance for all `inject` calls)
        module.bind<String>("STR")

        // You can use labels to inject different instances
        module.bind<String>("toolkit", "Hexagon")
        module.bind<Date>("+1h") { java.sql.Date(System.currentTimeMillis() + 3_600_000) }

        // Tags can be of *ANY* type
        module.bind<String>(0, "Zero")

        val currentSqlDate = injector.inject<Date>()
        val currentSqlDateInferredType: Date = injector.inject()

        // Inject different values for a class using tags (can be any type, not only string)
        val nextHourSqlDate: Date = injector.inject("+1h")
        val nextHourSqlDateInferredType: Date = injector.inject("+1h")

        // Injecting classes bound to objects return always the same instance
        val defaultString = injector.inject<String>()
        val taggedString: String = injector.inject("toolkit")
        val intTaggedString: String = injector.inject(0)

        // Overriding previously bound classes is not allowed
        try {
            module.bind<String>("STR Ignored")
        }
        catch (e: IllegalStateException) {
            logger.error { "String already has a generator bound. The program should abort." }
        }
        val ignoredBinding = injector.inject<String>()

        // You can overwrite previously bound classes using `forceBind*` methods
        module.forceBind<String>("STR Overridden")
        val overriddenBinding = injector.inject<String>()
        // injectionUsage

        val millis = System.currentTimeMillis()

        assert(currentSqlDate is java.sql.Date)
        assert(currentSqlDate.time <= millis)
        assert(nextHourSqlDate.time <= millis + 3_600_000)
        assert(currentSqlDateInferredType.time <= millis)
        assert(nextHourSqlDate is java.sql.Date)
        assert(nextHourSqlDateInferredType.time <= millis + 3_600_000)
        assertEquals("STR", defaultString)
        assertEquals("STR", ignoredBinding)
        assertEquals("STR Overridden", overriddenBinding)
        assertEquals("Hexagon", taggedString)
        assertEquals("Zero", intTaggedString)
        assertSame(injector.inject<String>(), injector.inject<String>())
    }

    @Suppress("RemoveExplicitTypeArguments")
    @Test fun multipleBindingsInjectionUsage() {
        module.clear()

        // multipleBindingsInjectionUsage
        // This snippet assumes the following IMPORTS ARE DECLARED:
        // import com.hexagonkt.injection.InjectionManager.module
        // import com.hexagonkt.injection.InjectionManager.injector

        // Bind classes to functions (create a different instance with each `inject` call)
        module.bindGenerators<Date>({ java.sql.Date(System.currentTimeMillis()) })

        // Bind classes to objects (returns the same instance for all `inject` calls)
        module.bind<String>("STR")

        // You can use labels to inject different instances
        module.bind<String>("toolkit", "Hexagon")
        module.bind<Date>("+1h") { java.sql.Date(System.currentTimeMillis() + 3_600_000) }

        // Tags can be of *ANY* type
        module.bind<String>(0, "Zero")

//        val currentSqlDate = injector.inject<Date>()
//        val currentSqlDateInferredType: Date = injector.inject()
//
//        // Inject different values for a class using tags (can be any type, not only string)
//        val nextHourSqlDate: Date = injector.inject("+1h")
//        val nextHourSqlDateInferredType: Date = injector.inject("+1h")
//
//        // Injecting classes bound to objects return always the same instance
//        val defaultString = injector.inject<String>()
//        val taggedString: String = injector.inject("toolkit")
//        val intTaggedString: String = injector.inject(0)
//
//        // Overriding previously bound classes is not allowed
//        try {
//            module.bind<String>("STR Ignored")
//        }
//        catch (e: IllegalStateException) {
//            logger.error { "String already has a generator bound. The program should abort." }
//        }
//        val ignoredBinding = injector.inject<String>()
//
//        // You can overwrite previously bound classes using `forceBind*` methods
//        module.forceBind(String::class, "STR Overridden")
//        val overriddenBinding = injector.inject<String>()
//        // multipleBindingsInjectionUsage
//
//        val millis = System.currentTimeMillis()
//
//        assert(currentSqlDate is java.sql.Date)
//        assert(currentSqlDate.time <= millis)
//        assert(nextHourSqlDate.time <= millis + 3_600_000)
//        assert(currentSqlDateInferredType.time <= millis)
//        assert(nextHourSqlDate is java.sql.Date)
//        assert(nextHourSqlDateInferredType.time <= millis + 3_600_000)
//        assertEquals("STR", defaultString)
//        assertEquals("STR", ignoredBinding)
//        assertEquals("STR Overridden", overriddenBinding)
//        assertEquals("Hexagon", taggedString)
//        assertEquals("Zero", intTaggedString)
//        assertSame(injector.inject<String>(), injector.inject<String>())
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
