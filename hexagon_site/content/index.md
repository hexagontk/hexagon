title=Hexagon
date=2016-04-13
type=page
status=published
~~~~~~


HEXAGON ${projectVersion}
=========================
### The atoms of your platform

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

![coverage](https://codecov.io/gh/hexagonkt/hexagon/branch/master/graphs/tree.svg)

[Kotlin]: http://kotlinlang.org
[RabbitMQ]: http://www.rabbitmq.com
[MongoDB]: https://www.mongodb.com

## Getting started

For detailed information about how to create a service, please refer to the [Service Life Cycle]
documentation.

You can create a service from a [Lazybones] template. To do so type:
`lazybones create hexagon-service service`

Or you can write a [Gradle] project from scratch (Gradle 3 is required):

`build.gradle`:

```groovy
buildscript {
    repositories { jcenter () }
    dependencies { classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.0.6" }
}

apply plugin: "kotlin"
apply plugin: "application"

mainClassName = 'HelloKt'

repositories { jcenter () }

dependencies {
    compile ("com.hexagonkt:hexagon_core:0.21.0")
    compile ("org.eclipse.jetty:jetty-webapp:9.3.16.v20170120")
}
```

`src/main/kotlin/Hello.kt`:

```kotlin
import com.hexagonkt.server.*

fun main(args: Array<String>) {
    get("/hello/{name}") { ok("Hello ${request["name"]}!") }
    run()
}
```

Now you can run the service with `gradle run` and view the results at:
[http://localhost:2010/hello/world](http://localhost:2010/hello/world)

[Lazybones]: https://github.com/pledbrook/lazybones
[Gradle]: https://gradle.org/

## Further resources

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

[Service Life Cycle]: life_cycle.html
[HTTP]: rest.html
[Serialization]: serialization.html
[Storage]: storage.html
[Events]: events.html
[Configuration]: configuration.html
[Templates]: templates.html
[Scheduling]: scheduling.html
[Testing]: testing.html

## Build and Contribute

Refer to the [contribute] section for detailed information about Hexagon's development.

[contribute]: contribute.html

