
# Module helpers
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

    implementation("com.hexagontk:helpers:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagontk</groupId>
      <artifactId>helpers</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

# Package com.hexagontk.helpers
Platform information and other useful utilities. Includes basic program settings support at the
Platform object (like loading and retrieving system settings).

# Package com.hexagontk.helpers.text
Text utilities to allow the use of ANSI escape codes and case converting tools among other features.
