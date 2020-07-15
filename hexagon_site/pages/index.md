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
    applications (Web applications, APIs or queue consumers) that run inside a cloud platform.
  </p>
---

The Hexagon Toolkit provides several libraries to build server applications. These libraries provide
single standalone features[^1] and are referred to as ["Ports"][Ports and Adapters Architecture].

The main ports are:

* [The HTTP server]: supports nested routers, grouped routes, static files, forms processing,
* [The HTTP client]: which supports mutual TLS, HTTP/2, cookies

The toolkit's ports are designed to work on their own. For example: you can use the `http_server`
module without importing the `templates` one, and the other way around.

Each of these features or ports may have different implementations called
["Adapters"][Ports and Adapters Architecture]. Clients should only use ports code, this makes easy
to switch among different ports' adapters with minimum impact.

[Core utilities]: like settings handling, logging, serialization and dependency injection[^2].
The toolkit's ports are designed to use core functionalities. You can use a third party DI library
instead using the Core one.

Hexagon is designed to fit in applications that conform to the [Hexagonal Architecture] (also called
[Clean Architecture] or [Ports and Adapters Architecture]). Its design principles also fits in this
architecture.

[^1]: Except the Core module that contains a set of utilities like serialization and dependency
injection among others.
[^2]: You are not going to use it a lot.

[The HTTP server]: /port_http_server/index.html
[The HTTP client]: /port_http_client/index.html
[Core utilities]: /hexagon_core/index.html
[Hexagonal Architecture]: http://fideloper.com/hexagonal-architecture
[Clean Architecture]: https://8thlight.com/blog/uncle-bob/2012/08/13/the-clean-architecture.html
[Ports and Adapters Architecture]: https://herbertograca.com/2017/09/14/ports-adapters-architecture

# Hello World

Simple Hello World HTTP example.

@sample http_server_jetty/src/test/kotlin/HelloWorld.kt

You can check the [code examples] and [demo projects] for more complex use cases.

[code examples]: /examples/http_server_examples
[demo projects]: /examples/example_projects

# Features

Hexagon's goals and design principles:

* [Simple to Use](/quick_start): Hexagon is focused in allowing you to use the features you use the
  most in your daily coding in the easiest way.
> 6. No complicated things. Easy to hack, easy to understand (no source code generation, no
>    annotation processing)

* [Pluggable Adapters](/developer_guide): Adding an adapter is just implementing the port's
  interface. You can code your own adapters from scratch or tune the existing ones.
> 3. Easily change underlying technologies, a thin layer always impact flexibility and performance
>    but usually is more important to adapt fast than ease common tasks at the expense of a little
>    performance. Swap adapters without changing application code.

* [Kotlin First](http://kotlinlang.org): The library is coded in Kotlin for coding with Kotlin. No
  strings attached to Java (as a Language).
> 2. Take advantage of Kotlin, this new language makes previous Java problems not existent. I.e.:
>    DI is hard to implement in Java, but it is a breeze to do in Kotlin

* [Properly Tested](https://github.com/hexagonkt/hexagon#status): Project's coverage is checked in
  every Pull Request. It is also stress tested at [TechEmpower Frameworks Benchmark][benchmark].

* [Modular](/developer_guide): Each feature is isolated in its own module. Use only the modules you
  need without unneeded dependencies.
> 1. Modular (take only 3rd party libraries you need). I.e.: you can use templates without using the
>    http server

> 5. All minimum pieces to make production services (HTTP servers, Queue consumers, etc):
>    settings, serialization...
>    Have all minimum app development requirements together (settings, di, serialization)
>    "Batteries included"
> 7. Toolkit not framework. You control your tools, not the other way around.

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
| [Settings]              | [Environment], [Files], [Resources]

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
[YAML]: /hexagon_core/com.hexagonkt.serialization/-yaml
[Settings]: /hexagon_core/#settings
[Environment]: /hexagon_core/com.hexagonkt.settings/-environment-variables-source
[Files]: /hexagon_core/com.hexagonkt.settings/-file-source
[Resources]: /hexagon_core/com.hexagonkt.settings/-resource-source
