
# Module templates_freemarker
This module provides an adapter for the templates Port supporting the Apache [FreeMarker] template
engine.

For usage instructions, refer to the [Templates Port documentation](/templates/).

[FreeMarker]: https://freemarker.apache.org

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagonkt:templates_freemarker:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>templates_freemarker</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

# Package com.hexagonkt.templates.freemarker
Classes that implement the Templates Port interface with the [FreeMarker] engine.
