
# Module http_server_netty
[Netty] adapter for the [http_server] port.

[Netty]: https://netty.io
[http_server]: http_server.md

### Install the Dependency

=== "build.gradle"

    ```groovy
    dependencies {
      implementation("com.hexagontk.http:http_server_netty:$hexagonVersion")
    }
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagontk.http</groupId>
      <artifactId>http_server_netty</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

### epoll

=== "build.gradle"

    ```groovy
    dependencies {
      implementation("com.hexagontk.http:http_server_netty_epoll:$hexagonVersion")
      // $arch could be 'x86_64' among other linux architectures
      implementation("io.netty:netty-transport-native-epoll:$nettyVersion:linux-$arch")
    }
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagontk.http</groupId>
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

### io_uring

=== "build.gradle"

    ```groovy
    dependencies {
      implementation("com.hexagontk.http:http_server_netty_io_uring:$hexagonVersion")
      // $arch could be 'x86_64' among other linux architectures
      implementation("io.netty:netty-transport-native-io_uring:$nettyVersion:linux-$arch")
    }
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagontk.http</groupId>
      <artifactId>http_server_netty_io_uring</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    <dependency>
      <groupId>io.netty</groupId>
      <artifactId>netty-transport-native-io_uring</artifactId>
      <version>$nettyVersion</version>
      <!-- $arch could be 'x86_64' among other linux architectures -->
      <classifier>linux-$arch</classifier>
    </dependency>
    ```

# Package com.hexagontk.http.server.netty
Code implementing the Netty HTTP server adapter.

# Package com.hexagontk.http.server.netty.epoll
Code implementing the Netty HTTP server adapter using Epoll transport.

# Package com.hexagontk.http.server.netty.io.uring
Code implementing the Netty HTTP server adapter using io_uring transport.
