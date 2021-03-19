
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

    // Add `{ exclude(module = "logging_slf4j_jul") }` if you use other logging adapter
    implementation("com.hexagonkt:http_server_jetty:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>http_server_jetty</artifactId>
      <version>$hexagonVersion</version>
      <!-- Add the following block if you use other logging adapter -->
      <!--
      <exclusions>
        <exclusion>
          <groupId>sample.ProjectB</groupId>
          <artifactId>Project-B</artifactId>
        </exclusion>
      </exclusions>
      -->
    </dependency>
    ```

!!! Note
    This Adapter includes the [logging_slf4j_jul] logging dependency (for convenience), if you use a
    different log adapter, you should exclude it to avoid [SLF4J] warnings.

[logging_slf4j_jul]: /logging_slf4j_jul
[SLF4J]: http://www.slf4j.org

# Package com.hexagonkt.http.server.jetty

Code implementing the Jetty HTTP server adapter.
