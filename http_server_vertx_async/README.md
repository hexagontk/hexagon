
# Module http_server_vertx_async
[Vert.x] adapter for the [http_server] port.

[Vert.x]: https://vertx.io
[http_server]: /http_server

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    dependencies {
      implementation("com.hexagonkt:http_server_vertx_async:$hexagonVersion")
    }
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>http_server_vertx_async</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

# Package com.hexagonkt.http.server.vertx
Code implementing the Vert.x HTTP server adapter.
