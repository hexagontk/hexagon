
# Module hexagon_core

This module holds utilities used in other libraries of the toolkit. Check the packages'
documentation for more details. You can find a quick recap of the main features in the sections
below.

### Logger

The following code block shows the most common use cases for the [Logger] class:

@sample hexagon_core/src/test/kotlin/com/hexagonkt/HexagonCoreSamplesTest.kt:logger

[Logger]: com.hexagonkt.helpers/-logger/index.md

### Dependency injection

You can take advantage of dependency injection using the [InjectionManager] object.

Check this sample to bind constructor functions or objects to classes, and inject them later:

@sample hexagon_core/src/test/kotlin/com/hexagonkt/HexagonCoreSamplesTest.kt:injectionUsage

[InjectionManager]: com.hexagonkt.injection/-injection-manager/index.md

### Serialization

The core module has utilities to serialize/parse data classes to JSON and YAML. Read the following
snippet for details:

@sample hexagon_core/src/test/kotlin/com/hexagonkt/HexagonCoreSamplesTest.kt:serializationUsage

### Settings

@sample hexagon_core/src/test/kotlin/com/hexagonkt/HexagonCoreSamplesTest.kt:settingsUsage

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

