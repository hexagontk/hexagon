
# Module http_server_jdk
[JDK] adapter for the [http_server] port.

[JDK]: https://docs.oracle.com/en/java/javase/21/docs/api/jdk.httpserver/module-summary.html
[http_server]: http_server.md

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    dependencies {
      implementation("com.hexagontk.http:http_server_jdk:$hexagonVersion")
    }
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagontk.http</groupId>
      <artifactId>http_server_jdk</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

# Package com.hexagontk.http.server.jdk
Code implementing the JDK HTTP server adapter.
