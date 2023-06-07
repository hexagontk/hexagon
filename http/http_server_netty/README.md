
# Module http_server_netty
[Netty] adapter for the [http_server] port.

[Netty]: https://netty.io
[http_server]: /http_server

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    dependencies {
      implementation("com.hexagonkt:http_server_netty:$hexagonVersion")
    }
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>http_server_netty</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

# Package com.hexagonkt.http.server.netty
Code implementing the Netty HTTP server adapter.
