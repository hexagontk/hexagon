package com.hexagonkt.helpers

import com.hexagonkt.injection.InjectionManager
import org.testng.annotations.Test
import java.lang.IllegalStateException
import java.util.*

class HelpersSamplesTest {

    @Suppress("RedundantExplicitType")
    @Test fun loggerUsage() {
        // logger
        val classLogger: Logger = Logger(Runtime::class) // Logger for the `Runtime` class
        val instanceLogger: Logger = Logger(this) // Logger for this instance's class

        logger.info {
            """
            You can add a quick log without declaring a Logger using 'com.hexagonkt.helpers.logger'.
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
        // injectionUsage
        // Bind classes to functions (create a different instance with each `inject` call)
        InjectionManager.bind<Date> { java.sql.Date(System.currentTimeMillis()) }

        // Bind classes to objects (returns the same instance for all `inject` calls)
        InjectionManager.bindObject<String>("STR")

        // You can use labels to inject different instances
        InjectionManager.bind<Date>("+1h") { java.sql.Date(System.currentTimeMillis() + 3_600_000) }
        InjectionManager.bindObject<String>("toolkit", "Hexagon")

        val currentSqlDate = InjectionManager.inject<Date>()
        val currentSqlDateInferredType: Date = InjectionManager.inject()

        // Inject different values for a class using tags (can be any type, not only string)
        val nextHourSqlDate: Date = InjectionManager.inject("+1h")
        val nextHourSqlDateInferredType: Date = InjectionManager.inject("+1h")

        // Injecting classes bound to objects return always the same instance
        val defaultString = InjectionManager.inject<String>()
        val taggedString: String = InjectionManager.inject("toolkit")
        // injectionUsage

        val millis = System.currentTimeMillis()

        assert(currentSqlDate is java.sql.Date)
        assert(currentSqlDate.time == millis)
        assert(currentSqlDateInferredType.time <= millis)
        assert(nextHourSqlDate is java.sql.Date)
        assert(nextHourSqlDate.time == millis + 3_600_000)
        assert(nextHourSqlDateInferredType.time <= millis + 3_600_000)
        assert(defaultString == "STR")
        assert(taggedString == "Hexagon")
        assert(InjectionManager.inject<String>() === InjectionManager.inject<String>())
    }
}
