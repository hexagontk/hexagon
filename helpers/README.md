
# Module helpers
This module holds utilities not used in other libraries of the toolkit, but useful for client
applications. Check the packages' documentation for more details.

## Install the Dependency
If you want to use these utilities, you can import this module with the following code:

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
Utilities to check fields, run programs or shell command, I18n, etc.

# Package com.hexagontk.helpers.text
Text utilities like case converting tools.
