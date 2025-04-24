
# Module http_client_jetty
[http_client] implementation using the [Jetty HTTP Client] library.

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

    implementation("com.hexagontk.http:http_client_jetty:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagontk.http</groupId>
      <artifactId>http_client_jetty</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

# Package com.hexagontk.http.client.jetty
Jetty HTTP client implementation classes.

# Package com.hexagontk.http.client.jetty.ws
Jetty HTTP client implementation classes (with WebSockets support).
