
# Module http_server_jetty
[Jetty] adapter for the [http_server] port.

[Jetty]: https://www.eclipse.org/jetty
[http_server]: http_server.md

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    dependencies {
      implementation("com.hexagontk:http_server_jetty:$hexagonVersion")
    }
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagontk</groupId>
      <artifactId>http_server_jetty</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

# Package com.hexagontk.http.server.jetty
Code implementing the Jetty HTTP server adapter.
