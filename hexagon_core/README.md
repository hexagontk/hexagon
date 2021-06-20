
# Module hexagon_core

This module holds utilities used in other libraries of the toolkit. Check the packages'
documentation for more details. You can find a quick recap of the main features in the sections
below.

### Install the Dependency

This module is not meant to be imported directly. It will be included by using any other part of the
toolkit. However, if you only want to use the utilities, logging or dependency injection (i.e.: for
a desktop application), you can import it with the following code:

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagonkt:hexagon_core:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>hexagon_core</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

### Defined Ports

TODO Document module exposed ports (they may be initialized through dependency injection):
* LoggingPort
* Mapper
* SerializationFormat

### URLs

TODO Note that GraalVM requires to install the Classpath handler manually with
`ClasspathHandlerProvider.registerHandler`.

### JVM Information

TODO Add information about the system property that disables JMX.

### Logger

The following code block shows the most common use cases for the [Logger] class:

@code hexagon_core/src/test/kotlin/HexagonCoreSamplesTest.kt:logger

By default, Hexagon uses the [Java Util Logging] logging library, you can use any of its
implementations by just adding another logging adapter as a dependency. Below you can see some
alternatives:

* [Logback](/logging_logback)
* [SLF4J JUL](/logging_slf4j_jul)

TODO Add `LoggingManager` examples for changing logging level

[Logger]: api/hexagon_core/hexagon_core/com.hexagonkt.logging/-logger/index.html
[Java Util Logging]:
  https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html

### Dependency injection

You can take advantage of dependency injection using the [InjectionManager] object.

The implementation is a map of classes (with an optional tag) to provider functions (in essence:
`Map<KClass<*>, () -> Any>`). It is a very simple, yet complete, DI implementation.

You can bind supplier functions or objects to classes. If a class is already bound, later calls to
`bind*` methods are ignored. However, you can use the `forceBind*` methods if you need to override
a binding (in tests for example).

Check this sample to bind constructor functions or objects to classes, and inject them later:

@code hexagon_core/src/test/kotlin/HexagonCoreSamplesTest.kt:injectionUsage

!!! Info
    Dependency Injection is not required by the toolkit. All classes and methods have versions
    receiving all of their dependencies, so you can use them instead relying on injection (or use
    another DI library of your choice).

[InjectionManager]: api/hexagon_core/hexagon_core/com.hexagonkt.injection/-injection-manager/index.html

### Serialization

The core module has utilities to serialize/parse data classes to JSON and YAML. Read the following
snippet for details:

@code hexagon_core/src/test/kotlin/HexagonCoreSamplesTest.kt:serializationUsage

# Package com.hexagonkt.helpers

JVM information, a logger class and other useful utilities.

# Package com.hexagonkt.injection

Utilities to bind classes to creation closures or instances, and inject instances of those classes
later.

# Package com.hexagonkt.logging

Provides a logging management capabilities abstracting the application from logging libraries.

# Package com.hexagonkt.serialization

Parse/serialize data in different formats to class instances.
