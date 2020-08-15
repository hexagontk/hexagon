
# Module http_server_jetty

[Jetty] adapter for the [port_http_server] port.

[Jetty]: https://www.eclipse.org/jetty
[port_http_server]: /port_http_server

### Install the Dependency

```groovy tab="build.gradle"
repositories {
    mavenCentral()
}

implementation("com.hexagonkt:http_server_jetty:$hexagonVersion")
```

```xml tab="pom.xml"
<dependency>
  <groupId>com.hexagonkt</groupId>
  <artifactId>http_server_jetty</artifactId>
  <version>$hexagonVersion</version>
</dependency>
```

# Package com.hexagonkt.http.server.jetty

Code implementing the Jetty HTTP server adapter.
