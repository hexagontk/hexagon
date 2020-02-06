---
hero: |
  <p align="center">
    <img alt="Hexagon" src="tile-small.png" />
    <br />
    <a href="https://travis-ci.org/hexagonkt/hexagon">
      <img src="https://travis-ci.org/hexagonkt/hexagon.svg?branch=master" alt="Travis CI" />
    </a>
    <a href="https://sonarcloud.io/dashboard?id=hexagonkt_hexagon">
      <img
        src=
         "https://sonarcloud.io/api/project_badges/measure?project=hexagonkt_hexagon&metric=alert_status"
        alt="SonarQube" />
    </a>
    <a href="https://bintray.com/jamming/maven/hexagon_core/_latestVersion">
      <img
        src="https://api.bintray.com/packages/jamming/maven/hexagon_core/images/download.svg"
        alt="Bintray" />
    </a>
  </p>

  <h1 align="center">The atoms of your platform</h1>

  <p align="center" id="description">
    Hexagon is a microservices
    <a href="https://www.quora.com/Whats-the-difference-between-a-library-and-a-framework">
    toolkit</a> written in <a href="http://kotlinlang.org">Kotlin</a>. Its purpose is to ease the
    building of services (Web applications, APIs or queue consumers) that run inside a cloud
    platform
  </p>
---

The Hexagon toolkit is meant to provide abstraction from underlying technologies (data storage, HTTP
server engines, etc.) to be able to change them with minimum impact.

It is designed to fit in applications that conform to the [Hexagonal Architecture] (also called
[Clean Architecture] or [Ports and Adapters Architecture]).

* To see some simple examples you can read the [Quick Start] page.
* For more information you can check the [Developer Guide].
* You can read the [Core] or [HTTP Server] modules documentation.

[Hexagonal Architecture]: http://fideloper.com/hexagonal-architecture
[Clean Architecture]: https://8thlight.com/blog/uncle-bob/2012/08/13/the-clean-architecture.html
[Ports and Adapters Architecture]: https://herbertograca.com/2017/09/14/ports-adapters-architecture
[Quick Start]: /quick_start
[Developer Guide]: /developer_guide
[Core]: /hexagon_core
[HTTP Server]: /port_http_server

# Hello World

Simple Hello World HTTP example.

@sample hexagon_starters/src/main/kotlin/Service.kt

# Features

Hexagon's high-level features.

* [Simple to Use](/quick_start): Hexagon is focused in allowing you to use the features you use the
  most in your daily coding in the easiest way.

* [Easy to Hack](https://github.com/hexagonkt/hexagon/blob/master/contributing.md): The library is
  done to be lean and simple so you can tweak it to suit your needs instead relying on third
  parties.

* [Pluggable Adapters](/developer_guide): Adding an adapter is just implementing the port's
  interface. You can code your own adapters from scratch or tune the existing ones.

* [Kotlin First](http://kotlinlang.org): The library is coded in Kotlin for coding with Kotlin. No
  strings attached to Java (as a Language).

* [Properly Tested](https://github.com/hexagonkt/hexagon#status): Project's coverage is checked in
  every Pull Request. It is also stress tested at [TechEmpower Frameworks Benchmark][benchmark].

* [Modular](/developer_guide): Each feature is isolated in its own module. Use only the modules you
  need without unneeded dependencies.

[benchmark]: https://www.techempower.com/benchmarks

# Architecture

How Hexagon fits in your architecture in a picture.

![architecture](/img/architecture.svg)

# Ports

Ports with their provided implementations (Adapters).

| PORT                    | ADAPTERS
|-------------------------|---------
| [HTTP Server]           | [Jetty], [Servlet]
| [Templates]             | [Pebble]
| [Serialization Formats] | [JSON], [YAML]
| [Settings]              | [Environment], [Files], [Resources]

[Jetty]: /http_server_jetty
[Servlet]: /http_server_servlet
[Templates]: /port_templates
[Pebble]: /templates_pebble
[Serialization Formats]: /hexagon_core/#serialization
[JSON]: /hexagon_core/com.hexagonkt.serialization/-json
[YAML]: /hexagon_core/com.hexagonkt.serialization/-yaml
[Settings]: /hexagon_core/#settings
[Environment]: /hexagon_core/com.hexagonkt.settings/-environment-variables-source
[Files]: /hexagon_core/com.hexagonkt.settings/-file-source
[Resources]: /hexagon_core/com.hexagonkt.settings/-resource-source
