
# Module http_client_jetty_ws
[http_client] implementation using the [Jetty HTTP Client] library (adding WS support).

> ️⚠️ **Warning**
>
> <sup>WebSockets are not supported on GraalVM native images at the moment.</sup>

[http_client]: /http_client
[Jetty HTTP Client]: https://www.eclipse.org/jetty/documentation/jetty-11/programming-guide

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagonkt:http_client_jetty_ws:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>http_client_jetty_ws</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

# Package com.hexagonkt.http.client.jetty
Jetty HTTP client implementation classes.
