
# Module http_client_jdk
[http_client] implementation using the [Java HTTP Client] classes.

[http_client]: http_client.md
[Java HTTP Client]: https://docs.oracle.com/en/java/javase/21/docs/api/java.net.http/java/net/http/HttpClient.html

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagontk.http:http_client_jdk:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagontk.http</groupId>
      <artifactId>http_client_jdk</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

# Package com.hexagontk.http.client.jdk
Java HTTP client implementation classes.
