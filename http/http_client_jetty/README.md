
# Module http_client_jetty
[http_client] implementation using the [Jetty HTTP Client] library.

[http_client]: http_client.md
[Jetty HTTP Client]: https://jetty.org/docs/jetty/12/programming-guide

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagontk:http_client_jetty:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagontk</groupId>
      <artifactId>http_client_jetty</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

# Package com.hexagontk.http.client.jetty
Jetty HTTP client implementation classes.
