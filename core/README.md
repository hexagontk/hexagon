
# Module core

This module holds utilities used in other libraries of the toolkit. Check the packages'
documentation for more details. You can find a quick recap of the main features in the sections
below.

### Install the Dependency

This module is not meant to be imported directly. It will be included by using any other part of the
toolkit. However, if you only want to use the utilities, logging or serialization (i.e., for a
desktop application), you can import it with the following code:

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagonkt:core:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>core</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

### Defined Ports

TODO Document module exposed ports:
* LoggingPort

### URLs

TODO Note that GraalVM requires to install the Classpath handler manually with
`ClasspathHandlerProvider.registerHandler`.

### JVM Information

TODO Add information about the system property that disables JMX.

### Logger

The following code block shows the most common use cases for the [Logger] class:

@code core/src/test/kotlin/HexagonCoreSamplesTest.kt:logger

By default, Hexagon uses the [Java Util Logging] logging library, you can use any of its
implementations by just adding another logging adapter as a dependency. Below you can see some
alternatives:

* [Logback](/logging_logback)
* [SLF4J JUL](/logging_slf4j_jul)

TODO Add `LoggingManager` examples for changing logging level

[Logger]: /api/core/com.hexagonkt.logging/-logger/index.html
[Java Util Logging]:
  https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html

# Package com.hexagonkt.core.helpers

JVM information, a logger class and other useful utilities.

# Package com.hexagonkt.core.logging

Provides a logging management capabilities abstracting the application from logging libraries.

# Package com.hexagonkt.core.logging.jul

TODO

# Package com.hexagonkt.core.security

TODO
