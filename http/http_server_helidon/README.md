
# Module http_server_helidon
[Helidon] adapter for the [http_server] port.

## This adapter is in ALPHA state!

[Helidon]: https://helidon.io
[http_server]: /http_server

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    dependencies {
      implementation("com.hexagonkt:http_server_helidon:$hexagonVersion")
    }
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>http_server_helidon</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

# Package com.hexagonkt.http.server.helidon
Code implementing the Helidon HTTP server adapter.
