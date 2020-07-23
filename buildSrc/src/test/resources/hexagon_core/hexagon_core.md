
# Module hexagon_core

This module holds utilities used in other libraries of the toolkit. Check the packages'
documentation for more details. You can find a quick recap of the main features in the sections
below.

### Logger

The following code block shows the most common use cases for the [Logger] class:

```kotlin
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
```

[Logger]: com.hexagonkt.helpers/-logger/index.md

### Dependency injection

You can take advantage of dependency injection using the [InjectionManager] object.

You can bind supplier functions or objects to classes. If a class is already bound, later calls to
`bind*` methods are ignored. However, you can use the `forceBind*` methods if you need to override
a binding (in tests for example).

Check this sample to bind constructor functions or objects to classes, and inject them later:

```kotlin
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

// Overriding previously bound classes is not allowed (ignored)
InjectionManager.bindObject<String>("STR Ignored")
val ignoredBinding = InjectionManager.inject<String>()

// You can overwrite previously bound classes using `forceBind*` methods
InjectionManager.forceBindObject<String>("STR Overridden")
val overriddenBinding = InjectionManager.inject<String>()
```

[InjectionManager]: com.hexagonkt.injection/-injection-manager/index.md

### Serialization

The core module has utilities to serialize/parse data classes to JSON and YAML. Read the following
snippet for details:

```kotlin
val jason = Person("Jason", "Jackson", LocalDate.of(1989, 12, 31))

val jasonJson = jason.serialize(Json) // Can also be Yaml or an string: "application/json"
val parsedJason = jasonJson.parse(Person::class)

assert(jason == parsedJason)
assert(jason !== parsedJason)
```

### Settings

This module helps loading external settings from different sources. You can change the settings
sources, the default ones are (bottom sources override top ones):

Below there is a code fragment showing how to add a custom settings source and load its properties:

```kotlin
SettingsManager.settingsSources += ObjectSource(
    "stringProperty" to "str",
    "integerProperty" to 101,
    "booleanProperty" to true
)

assert(SettingsManager.settings["stringProperty"] == "str")
assert(SettingsManager.settings["integerProperty"] == 101)
assert(SettingsManager.settings["booleanProperty"] == true)
```

# Package com.hexagonkt.helpers

JVM information, a logger class and other useful utilities.

# Package com.hexagonkt.http

HTTP code shared between clients and servers and independent of third party libraries.

# Package com.hexagonkt.injection

Utilities to bind classes to creation closures or instances, and inject instances of those classes
later.

# Package com.hexagonkt.serialization

Parse/serialize data in different formats to class instances.

# Package com.hexagonkt.settings

Load settings from different data sources and formats.

