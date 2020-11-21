---
hero: |
  <p align="center">
    <img alt="Hexagon" src="tile-small.png" />
    <br />
    <a href="https://github.com/hexagonkt/hexagon/actions">
      <img
        alt="GitHub Actions"
        src="https://github.com/hexagonkt/hexagon/workflows/Release/badge.svg" />
    </a>
    <a href="/jacoco">
      <img src="/img/coverage.svg" alt="Coverage" />
    </a>
    <a href="https://search.maven.org/search?q=g:com.hexagonkt">
      <img src="/img/download.svg" alt="Maven Central Repository" />
    </a>
  </p>

  <h1 align="center">The atoms of your platform</h1>

  <p align="center" id="description">
    Hexagon is a microservices
    <a href="https://stackoverflow.com/a/3057818/973418">toolkit</a> written in
    <a href="http://kotlinlang.org">Kotlin</a>. Its purpose is to ease the building of server
    applications (Web applications, APIs, or queue consumers) that run inside a cloud platform.
  </p>
---

The Hexagon Toolkit provides several libraries to build server applications. These libraries provide
single standalone features[^1] and are referred to as ["Ports"][Ports and Adapters Architecture].

The main ports are:

* [The HTTP server]: supports HTTPS, HTTP/2, mutual TLS, static files (serve and upload), forms
  processing, cookies, sessions, CORS and more.
* [The HTTP client]: which supports mutual TLS, HTTP/2 and cookies among other features.

Each of these features or ports may have different implementations called
["Adapters"][Ports and Adapters Architecture].

Hexagon is designed to fit in applications that conform to the [Hexagonal Architecture] (also called
[Clean Architecture] or [Ports and Adapters Architecture]). Its design principles also fit into this
architecture.

[^1]: Except the Core module that contains a set of utilities like serialization and dependency
injection. However, some of these capacities can be replaced by other third party libraries.

[The HTTP server]: /port_http_server
[The HTTP client]: /port_http_client
[Core utilities]: /hexagon_core
[Hexagonal Architecture]: http://fideloper.com/hexagonal-architecture
[Clean Architecture]: https://8thlight.com/blog/uncle-bob/2012/08/13/the-clean-architecture.html
[Ports and Adapters Architecture]: https://herbertograca.com/2017/09/14/ports-adapters-architecture

# Hello World

Simple Hello World HTTP example.

@code http_server_jetty/src/test/kotlin/HelloWorld.kt

You can check the [code examples] and [demo projects] for more complex use cases.

[code examples]: /examples/http_server_examples
[demo projects]: /examples/example_projects

# Features

Hexagon's goals and design principles:

* **Put you in Charge**: There is no code generation, no runtime annotation processing, no classpath
  based logic, and no implicit behaviour. You control your tools, not the other way around.

* **Modular**: Each feature (Port) or adapter is isolated in its own module. Use only the modules
  you need without carrying unneeded dependencies.

* **Pluggable Adapters**: Every Port may have many implementations (Adapters) using different
  technologies. You can swap adapters without changing the application code.

* **Batteries Included**: It contains all the required pieces to make production-grade applications:
  settings management, serialization, dependency injection[^2] and build helpers.

* **Kotlin First**: Take full advantage of Kotlin instead of just calling Java code from Kotlin. The
  library is coded in Kotlin for coding with Kotlin. No strings attached to Java (as a Language).

* **Properly Tested**: The project's coverage is checked in every Pull Request. It is also
  stress-tested at [TechEmpower Frameworks Benchmark][benchmark].

[^2]: However, you can replace the core DI implementation with any other library.

[benchmark]: https://www.techempower.com/benchmarks

# Architecture

How Hexagon fits in your architecture in a picture.

!!! Note
    Using this toolkit won't make your application compliant with Hexagonal Architecture (by its
    nature, no tool can do that), you have to provide a layer of abstraction by yourself.

![architecture](/img/architecture.svg)

# Ports

Ports with their provided implementations (Adapters).

| PORT                    | ADAPTERS
|-------------------------|---------
| [HTTP Server]           | [Jetty], [Servlet]
| [HTTP Client]           | [AHC]
| [Messaging]             | [RabbitMQ]
| [Store]                 | [MongoDB]
| [Templates]             | [Pebble], [FreeMarker]
| [Serialization Formats] | [JSON], [YAML]
| [Settings]              | [Environment], [URLs], [System Properties]

[HTTP Server]: /port_http_server
[Jetty]: /http_server_jetty
[Servlet]: /http_server_servlet
[HTTP Client]: /port_http_client
[AHC]: /http_client_ahc
[Messaging]: /port_messaging
[RabbitMQ]: /messaging_rabbitmq
[Store]: /port_store
[MongoDB]: /store_mongodb
[Templates]: /port_templates
[Pebble]: /templates_pebble
[FreeMarker]: /templates_freemarker
[Serialization Formats]: /hexagon_core/#serialization
[JSON]: /hexagon_core/com.hexagonkt.serialization/-json
[YAML]: /serialization_yaml/com.hexagonkt.serialization/-yaml
[Settings]: /hexagon_settings
[Environment]: /hexagon_settings/com.hexagonkt.settings/-environment-variables-source
[URLs]: /hexagon_settings/com.hexagonkt.settings/-url-source
[System Properties]: /hexagon_settings/com.hexagonkt.settings/-system-properties-source
