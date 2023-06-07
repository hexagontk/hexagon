
# Module http_client_jetty
[http_client] implementation using the [Jetty HTTP Client] library.

[http_client]: /http_client
[Jetty HTTP Client]: https://www.eclipse.org/jetty/documentation/jetty-11/programming-guide

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagonkt:http_client_jetty:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>http_client_jetty</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

# Package com.hexagonkt.http.client.jetty
Jetty HTTP client implementation classes.
