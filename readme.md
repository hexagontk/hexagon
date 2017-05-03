[![BuildImg]][Build] [![CoverageImg]][Coverage] [![DownloadImg]][Download]

[BuildImg]: https://travis-ci.org/jaguililla/hexagon.svg?branch=master
[Build]: https://travis-ci.org/jaguililla/hexagon

[CoverageImg]: https://codecov.io/gh/jaguililla/hexagon/branch/master/graph/badge.svg
[Coverage]: https://codecov.io/gh/jaguililla/hexagon

[DownloadImg]: https://api.bintray.com/packages/jamming/maven/Hexagon/images/download.svg
[Download]: https://bintray.com/jamming/maven/Hexagon/_latestVersion

HEXAGON
=======
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

DISCLAIMER: The project status is beta. Use it at your own risk. This is the coverage grid:

[![CoverageGrid]][Coverage]

[CoverageGrid]: https://codecov.io/gh/jaguililla/hexagon/branch/master/graphs/tree.svg
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
    compile ("co.there4:hexagon:0.10.7")
    compile ("org.eclipse.jetty:jetty-webapp:9.3.16.v20170120")
}
```

`src/main/kotlin/Hello.kt`:

```kotlin
import co.there4.hexagon.server.*

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

[Service Life Cycle]: http://there4.co/hexagon/life_cycle.html
[HTTP]: http://there4.co/hexagon/rest.html
[Serialization]: http://there4.co/hexagon/serialization.html
[Storage]: http://there4.co/hexagon/storage.html
[Events]: http://there4.co/hexagon/events.html
[Configuration]: http://there4.co/hexagon/configuration.html
[Templates]: http://there4.co/hexagon/templates.html
[Scheduling]: http://there4.co/hexagon/scheduling.html
[Testing]: http://there4.co/hexagon/testing.html

## Build and Contribute

Refer to the [contribute] section for detailed information about Hexagon's development.

[contribute]: http://there4.co/hexagon/contribute.html

## License

MIT License

Copyright (c) 2016 Juanjo Aguililla

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
