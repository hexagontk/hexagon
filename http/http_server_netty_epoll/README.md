
# Module http_server_netty_epoll
[Netty] Epoll adapter for the [http_server] port.

[Netty]: https://netty.io
[http_server]: http_server.md

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    dependencies {
      implementation("com.hexagontk:http_server_netty_epoll:$hexagonVersion")
      // $arch could be 'x86_64' among other linux architectures
      implementation("io.netty:netty-transport-native-epoll:$nettyVersion:linux-$arch")
    }
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagontk</groupId>
      <artifactId>http_server_netty_epoll</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    <dependency>
      <groupId>io.netty</groupId>
      <artifactId>netty-transport-native-epoll</artifactId>
      <version>$nettyVersion</version>
      <!-- $arch could be 'x86_64' among other linux architectures -->
      <classifier>linux-$arch</classifier>
    </dependency>
    ```

# Package com.hexagontk.http.server.netty.epoll
Code implementing the Netty HTTP server adapter using Epoll transport.
