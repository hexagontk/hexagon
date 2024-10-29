
# Module http_server_helidon
[Helidon] adapter for the [http_server] port.

IMPORTANT: There is a [known bug] in this adapter.

[known bug]: https://github.com/hexagontk/hexagon/issues/704
[Helidon]: https://helidon.io
[http_server]: http_server.md

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    dependencies {
      implementation("com.hexagontk:http_server_helidon:$hexagonVersion")
    }
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagontk</groupId>
      <artifactId>http_server_helidon</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

# Package com.hexagontk.http.server.helidon
Code implementing the Helidon HTTP server adapter.
