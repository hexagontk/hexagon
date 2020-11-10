
# Module http_server_jetty

[Jetty] adapter for the [port_http_server] port.

[Jetty]: https://www.eclipse.org/jetty
[port_http_server]: /port_http_server

### Install the Dependency

=== "build.gradle"
  ```groovy
  repositories {
      mavenCentral()
  }

  implementation("com.hexagonkt:http_server_jetty:$hexagonVersion")
  ```
=== "pom.xml"
  ```xml
  <dependency>
    <groupId>com.hexagonkt</groupId>
    <artifactId>http_server_jetty</artifactId>
    <version>$hexagonVersion</version>
  </dependency>
  ```

# Package com.hexagonkt.http.server.jetty

Code implementing the Jetty HTTP server adapter.
