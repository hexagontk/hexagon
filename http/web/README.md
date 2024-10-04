
# Module web
Adds utilities for serving HTML pages over HTTP servers. Combines the [http_server] and [templates]
ports.

[http_server]: http_server.md
[templates]: templates.md

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagontk:web:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagontk</groupId>
      <artifactId>web</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

### Templates
Provide utilities for template processing inside HTTP handlers.

# Package com.hexagontk.web
TODO
