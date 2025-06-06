
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
    implementation("com.hexagontk:core:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagontk</groupId>
      <artifactId>core</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

# Package com.hexagontk.core
Platform information and other useful utilities. Includes basic program settings support at the
[Platform] object (like loading and retrieving system settings).

Provides a logging management capabilities abstracting the application from logging libraries.

The following code block shows the most common use cases for the logging utilities:

@code core/test/LoggersTest.kt?logger

By default, Hexagon uses the [System.Logger] class.

[Platform]: ../api/core/com.hexagontk.core/-platform
[System.Logger]: https://docs.oracle.com/javase/9/docs/api/java/lang/System.Logger.html

# Package com.hexagontk.core.media
Media types definitions and constants for default media types.

# Package com.hexagontk.core.security
Cryptography and key stores utilities.

# Package com.hexagontk.core.text
Text utilities to allow the use of ANSI escape codes among other features.
