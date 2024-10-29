
# Module http_client_jetty_ws
[http_client] implementation using the [Jetty HTTP Client] library (adding WS support).

> ️⚠️ **Warning**
>
> <sup>WebSockets are not supported on GraalVM native images at the moment.</sup>

[http_client]: http_client.md
[Jetty HTTP Client]: https://jetty.org/docs/jetty/12/programming-guide

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagontk:http_client_jetty_ws:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagontk</groupId>
      <artifactId>http_client_jetty_ws</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

# Package com.hexagontk.http.client.jetty
Jetty HTTP client implementation classes.
