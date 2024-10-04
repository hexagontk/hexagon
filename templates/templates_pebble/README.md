
# Module templates_pebble
[Pebble] template engine adapter for Hexagon.

For usage instructions, refer to the [Templates Port documentation](templates.md).

[Pebble]: https://pebbletemplates.io

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagontk:templates_pebble:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagontk</groupId>
      <artifactId>templates_pebble</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

# Package com.hexagontk.templates.pebble
Classes that implement the Templates Port interface with the [Pebble] engine.
