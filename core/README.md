
# Module core
This module holds utilities used in other libraries of the toolkit. Check the packages'
documentation for more details. You can find a quick recap of the main features in the sections
below.

## Install the Dependency
This module is not meant to be imported directly. It will be included by using any other part of the
toolkit. However, if you only want to use the utilities, logging, etc. (i.e., for a desktop
application), you can import it with the following code:

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

# Package com.hexagonkt.core
JVM information and other useful utilities.

## Classpath URLs in native images
To use the 'classpath' URL scheme on GraalVM native images, the `native-image` command requires to
add the Classpath handler manually with the `--enable-url-protocols=classpath` parameter.

## Flags (System Properties)
* DISABLE_CHECKS: set to true to disable some checks in order to shave a few ms. in production.
  Do not enable it in application development and turn it on only when the application is
  extensively tested.

# Package com.hexagonkt.core.handlers
Provide general utilities to attach many handlers to be applied on events processing. Events can be
of any type.

Handlers may or may not be applied to certain events at runtime based on rules provided as a
predicate (function).

If an event doesn't match a handler's predicate, that handler is ignored, and the next one will be
processed.

When a handler's predicate returns true, its callback will be called. That block will process the
event, and return a modified copy with the changes.

The callback block will decide if subsequent handler's will be evaluated by calling the next handler
explicitly (if `next` method is not called, the handlers defined later won't be evaluated).

There are types of handlers like 'After' y 'Before' that call `next` as part as their operation
discharging the user of doing so.

The events are passed to handlers' callbacks wrapped in a 'context'. The context can also pass
information among handlers in the `attributes` field. And store an exception if it is thrown in a
previous handler processing.

Concepts:

* Events
    * Context
* Handlers
    * Predicates
    * Callbacks

Types of handlers:

* On Handler: the callback is executed before calling the remaining handlers. `next` is called after
  the callback.
* After Handler: the predicate is evaluated after the remaining handlers return, and the callback is
  executed after. `next` is called before the callback
* Filter Handler: `next` is not called by this handler, its callback is responsible for doing so if
  needed.
* Chain Handler: this handler contains a list of handlers inside.

IMPORTANT: the order is NOT the order, it is the depth. Handlers are not linked, they are NESTED.
The `next()` method passes control to the next level.

This definition:

```
H1
H2
H3
```

Is really this execution:

```
H1 (on)
  H2 (on)
    H3 (on)
  H2 (after)
H1 (after)
```

# Package com.hexagonkt.core.logging
Provides a logging management capabilities abstracting the application from logging libraries.

The following code block shows the most common use cases for the [Logger] class:

@code core/src/test/kotlin/com/hexagonkt/core/HexagonCoreSamplesTest.kt?logger

By default, Hexagon uses the [Java Util Logging] logging library, you can use any of its
implementations by just adding another logging adapter as a dependency. Below you can see some
alternatives:

* [Logback](/logging_logback)
* [SLF4J JUL](/logging_slf4j_jul)

[Logger]: /api/core/com.hexagonkt.core.logging/-logger
[Java Util Logging]:
https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html

# Package com.hexagonkt.core.logging.jul
Logging implementation for Java logging module. This is the default implementation.

# Package com.hexagonkt.core.media
Media types definitions and constants for default media types.

# Package com.hexagonkt.core.security
Cryptography and key stores utilities.
