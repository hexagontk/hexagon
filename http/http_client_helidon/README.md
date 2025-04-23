
# Module http_client_helidon
[http_client] implementation using the [Helidon HTTP Client] library (adding WS support).

> ️⚠️ **Warning**
>
> <sup>WebSockets are not supported on GraalVM native images at the moment.</sup>

[http_client]: http_client.md
[Helidon HTTP Client]: https://helidon.io/docs/v4/se/webclient

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagontk.http:http_client_helidon:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagontk.http</groupId>
      <artifactId>http_client_helidon</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

# Package com.hexagontk.http.client.helidon
Helidon HTTP client implementation classes.
