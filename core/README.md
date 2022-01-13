
# Module core
This module holds utilities used in other libraries of the toolkit. Check the packages'
documentation for more details. You can find a quick recap of the main features in the sections
below.

### Install the Dependency
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

# Package com.hexagonkt.core.converters
Registry of functions to convert from one type to another.

TODO

# Package com.hexagonkt.core.handlers
TODO

KEY TAKEAWAY: the order is NOT the order, it is the depth. Handlers are not linked, they are NESTED.
The `next()` method passes control to the next level.

So this:

```
H1
H2
H3
```

Is really:

```
H1
  H2
    H3
  H2
H1
```

# Package com.hexagonkt.core.logging
Provides a logging management capabilities abstracting the application from logging libraries.

The following code block shows the most common use cases for the [Logger] class:

@code core/src/test/kotlin/HexagonCoreSamplesTest.kt:logger

By default, Hexagon uses the [Java Util Logging] logging library, you can use any of its
implementations by just adding another logging adapter as a dependency. Below you can see some
alternatives:

* [Logback](/logging_logback)
* [SLF4J JUL](/logging_slf4j_jul)

[Logger]: /api/core/com.hexagonkt.core.logging/-logger
[Java Util Logging]:
https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html

# Package com.hexagonkt.core.logging.jul
TODO

# Package com.hexagonkt.core.media
TODO

# Package com.hexagonkt.core.security
TODO
