
# Module web
Adds utilities for serving HTML pages over HTTP servers. Combines the [http_server] and [templates]
ports.

[http_server]: /http_server
[templates]: /templates

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagonkt:web:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>web</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

### Templates
Provide utilities for template processing inside HTTP handlers.

# Package com.hexagonkt.web
TODO
