![logo](hexagon_site/assets/tile-wide.png)
#### The atoms of your platform

[![BuildImg]][Build] [![CoverageImg]][Coverage] [![DownloadImg]][Download]

[BuildImg]: https://img.shields.io/travis/hexagonkt/hexagon.svg?colorA=0000FF&style=flat-square
[Build]: https://travis-ci.org/hexagonkt/hexagon

[CoverageImg]:
  https://img.shields.io/codecov/c/github/hexagonkt/hexagon.svg?colorA=0000FF&style=flat-square
[Coverage]: https://codecov.io/gh/hexagonkt/hexagon

[DownloadImg]:
  https://img.shields.io/bintray/v/jamming/maven/hexagon_core.svg?colorA=0000FF&style=flat-square
[Download]: https://bintray.com/jamming/maven/hexagon_core/_latestVersion

Hexagon is a microservices framework that doesn't follow the flock. It is written in [Kotlin] and
uses [RabbitMQ] and [MongoDB]. It takes care of:

* HTTP routing and HTML templates.
* Serialization and storage of domain classes.
* Asynchronous communication through events.
* Task scheduling using Cron expressions.

The purpose of the project is to provide a microservices framework with the following priorities
(in order):

1. Simple to use
2. Easily hackable
3. Be small

## Performance

Performance is not the primary goal, but it is taken seriously. You can check performance numbers
in the [TechEmpower Web Framework Benchmarks](https://www.techempower.com/benchmarks)

**DISCLAIMER**: The project status is beta. Use it at your own risk. This is the coverage grid:

[![CoverageGrid]][Coverage]

[CoverageGrid]: https://codecov.io/gh/hexagonkt/hexagon/branch/master/graphs/tree.svg
[Kotlin]: http://kotlinlang.org
[RabbitMQ]: http://www.rabbitmq.com
[MongoDB]: https://www.mongodb.com

## Getting Started

For detailed information about how to create a service, please refer to the [Service Life Cycle]
documentation.

You can create a service from a [Lazybones] template. To do so type:
`lazybones create hexagon-service service`

Or you can write a [Gradle] project from scratch (Gradle 3 is required):

`build.gradle`:

```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.1.2-2'
}

apply plugin: "kotlin"
apply plugin: "application"

mainClassName = 'HelloKt'

repositories {
    jcenter ()
}

dependencies {
    compile ("com.hexagonkt:server_jetty:0.14.0")
}
```

`src/main/kotlin/Hello.kt`:

```kotlin
import com.hexagonkt.server.*
import com.hexagonkt.server.jetty.*

fun main(vararg args: String) {
    serve(JettyServletEngine()) {
        get("/hello/{name}") { "Hello ${request["name"]}!" }
    }
}
```

Now you can run the service with `gradle run` and view the results at:
[http://localhost:2010/hello/world](http://localhost:2010/hello/world)

[Lazybones]: https://github.com/pledbrook/lazybones
[Gradle]: https://gradle.org/

## Further Resources

* [Service Life Cycle]: provide helpers to create, build and package your services.
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

[Service Life Cycle]: http://hexagonkt.com/life_cycle.html
[HTTP]: http://hexagonkt.com/rest.html
[Serialization]: http://hexagonkt.com/serialization.html
[Storage]: http://hexagonkt.com/storage.html
[Events]: http://hexagonkt.com/events.html
[Configuration]: http://hexagonkt.com/configuration.html
[Templates]: http://hexagonkt.com/templates.html
[Scheduling]: http://hexagonkt.com/scheduling.html
[Testing]: http://hexagonkt.com/testing.html

## Contribute

Refer to the [contributing.md](contributing.md) file for detailed information about Hexagon's
development.

## License

The project is licensed under the [MIT License](license.md).
