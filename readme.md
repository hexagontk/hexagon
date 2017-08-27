
<h1 align="center">
  <a href="http://hexagonkt.com">
    <img alt="Hexagon" src="hexagon_site/assets/tile-small.png" />
  </a>
  <br>
  Hexagon
</h1>

<h4 align="center">The atoms of your platform</h4>

<p align="center">
  <a href="https://travis-ci.org/hexagonkt/hexagon">
    <img
      src="https://img.shields.io/travis/hexagonkt/hexagon.svg?colorA=0073BB&style=flat-square" 
      alt="Travis CI" />
  </a>
  <a href="https://codecov.io/gh/hexagonkt/hexagon">
    <img
      src=
        "https://img.shields.io/codecov/c/github/hexagonkt/hexagon.svg?colorA=0073BB&style=flat-square"
      alt="Codecov" />
  </a>
  <a href="https://bintray.com/jamming/maven/hexagon_core/_latestVersion">
    <img
      src=
        "https://img.shields.io/bintray/v/jamming/maven/hexagon_core.svg?colorA=0073BB&style=flat-square"
      alt="Bintray" />
  </a>
</p>

<p align="center">
  <a href="http://hexagonkt.com">Quick Start</a> |
  <a href="http://hexagonkt.com">Guides</a> |
  <a href="http://hexagonkt.com">API Reference</a> |
  <a href="http://hexagonkt.com">Community</a>
</p>

---

Hexagon is a microservices framework that doesn't follow the flock. It is written in [Kotlin] and
its pursose is to ease the building of services (Web applications, APIs or Queue consumers). To 
achieve this goal, it takes care of:

* HTTP routing and HTML templates.
* Serialization and storage of data.
* Asynchronous communication through events.
* Task scheduling using Cron expressions.

The purpose of the project is to provide a microservices framework with the following priorities
(in order):

1. Simple to use: make it easy to develop user services fast.
2. Easy to hack: allow the user to add extensions or change the framework.
3. Be small: this is really a requirement for the previous points.

## Getting Started

1. Setup Kotlin in [Gradle][Setup Gradle] or [Maven][Setup Maven].
2. Setup the [JCenter repository](https://bintray.com/bintray/jcenter) (click on the `Set me up!`
   button).
3. Add the dependency:

  * In [Gradle]. Import it inside `build.gradle`:

    ```groovy
    compile ("com.hexagonkt:server_jetty:0.21.0")
    ```

  * In [Maven]. Declare the dependency in `pom.xml`:

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>server_jetty</artifactId>
      <version>0.21.0</version>
    </dependency>
    ```

4. Write the code in the `src/main/kotlin/Hello.kt` file:

    ```kotlin
    import com.hexagonkt.server.*
    import com.hexagonkt.server.jetty.*

    fun main(vararg args: String) {
        serve(JettyServletEngine()) {
            get("/hello/{name}") { "Hello ${request["name"]}!" }
        }
    }
    ```

5. Run the service and view the results at: [http://localhost:2010/hello/world][Endpoint]

You can read more details at the [Service Creation] guide.

[Setup Gradle]: https://kotlinlang.org/docs/reference/using-gradle.html
[Setup Maven]: https://kotlinlang.org/docs/reference/using-maven.html
[Endpoint]: http://localhost:2010/hello/world
[Maven]: https://maven.apache.org
[Gradle]: https://gradle.org

## Guides

* [Service Creation]: utilities to create, build and package your services.
* [HTTP]: Web routing and filters. It is handled like the [Sinatra] Ruby framework.
* [Serialization]: helper methods to serialize/deserialize `data classes` using different formats.
* [Storage]: utilities to persist Kotlin objects into [MongoDB] collections.
* [Events]: support asynchronous communication with events through the [RabbitMQ] message broker.
* [Configuration]: allow the configuration of the engine by using YAML files.
* [Scheduling]: supports the execution of tasks periodically using Cron expressions.
* [Templates]: allow the service to render results using [Pebble] or [kotlinx.html].
* [Testing]: Hexagon adds utilities to ease the testing of its services.

[Sinatra]: http://sinatrarb.com
[Pebble]: http://www.mitchellbosecke.com/pebble/home
[kotlinx.html]: https://github.com/Kotlin/kotlinx.html

[Service Creation]: http://hexagonkt.com/life_cycle.html
[HTTP]: http://hexagonkt.com/rest.html
[Serialization]: http://hexagonkt.com/serialization.html
[Storage]: http://hexagonkt.com/storage.html
[Events]: http://hexagonkt.com/events.html
[Configuration]: http://hexagonkt.com/configuration.html
[Templates]: http://hexagonkt.com/templates.html
[Scheduling]: http://hexagonkt.com/scheduling.html
[Testing]: http://hexagonkt.com/testing.html

## Status

**DISCLAIMER**: The project status is beta. Use it at your own risk. There are some modules not
started yet (ie: service registry) and the API is subject to change any time prior to release 1.0.

Performance is not the primary goal, but it is taken seriously. You can check performance numbers
in the [TechEmpower Web Framework Benchmarks](https://www.techempower.com/benchmarks)

This is the coverage grid:

[![CoverageGrid]][Coverage]

[CoverageGrid]: https://codecov.io/gh/hexagonkt/hexagon/branch/master/graphs/icicle.svg
[Coverage]: https://codecov.io/gh/hexagonkt/hexagon
[Kotlin]: http://kotlinlang.org
[RabbitMQ]: http://www.rabbitmq.com
[MongoDB]: https://www.mongodb.com

## Contribute

Refer to the [contributing.md](contributing.md) file for detailed information about Hexagon's
development.

[Project board](https://github.com/hexagonkt/hexagon/projects/1)

[Slack channel](https://kotlinlang.slack.com/messages/hexagon)

Eventually I will thank all [contributors], but now it's just [me].

[contributors]: https://github.com/hexagonkt/hexagon/graphs/contributors
[me]: https://github.com/jaguililla

## License

The project is licensed under the [MIT License](license.md).
