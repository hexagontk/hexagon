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

* [Service Life Cycle]: provide helpers to build, run, package and deploy your service.
* [rest]
* [serialization]: TODO Write documentation
* [storage]: TODO Write documentation
* [events]
* [configuration]: TODO Partial implementation
* [templates]: TODO Partial implementation
* [scheduling]
* [testing]: TODO Write documentation

The purpose of the project is to provide a micro services framework with the following priorities
(in order):

1. Simple to use
2. Easily hackable
3. Be small

The name and logo are an hexagon because it is the usual way of representing a microservice in a
diagram.

DISCLAIMER: The project status right now is beta. Use it at your own risk

[Kotlin]: http://kotlinlang.org
[RabbitMQ]: http://www.rabbitmq.com
[MongoDB]: https://www.mongodb.com

[Service Life Cycle]: http://there4.co/hexagon/life_cycle.html
[rest]: http://there4.co/hexagon/rest.html
[serialization]: http://there4.co/hexagon/serialization.html
[storage]: http://there4.co/hexagon/storage.html
[events]: http://there4.co/hexagon/events.html
[configuration]: http://there4.co/hexagon/configuration.html
[templates]: http://there4.co/hexagon/templates.html
[scheduling]: http://there4.co/hexagon/scheduling.html
[testing]: http://there4.co/hexagon/testing.html

## Getting started

You can create a service from a [Lazybones] template. Or writing a [Gradle] script. Check the
[Service Life Cycle] for more information.

Get the dependency from [JCenter] (you need to [setup the repository] first):

Minimal `build.gradle` example:

```groovy
buildscript {
    repositories { jcenter () }
    dependencies { classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.0.6" }
}

apply plugin: "kotlin"

repositories { jcenter () }

dependencies {
    compile ("co.there4:hexagon:0.10.3")
    compile ("org.eclipse.jetty:jetty-webapp:9.3.14.v20161028")
}
```

Write the code (ie: `src/main/kotlin/Hello.kt`):

```kotlin
import co.there4.hexagon.web.*

fun main(args: Array<String>) {
    get("/hello/{name}") { ok("Hello ${request["name"]}!") }
    run()
}
```

Launch it and view the results at: [http://localhost:2010/hello]

[JCenter]: https://bintray.com/jamming/maven/Hexagon
[setup the repository]: https://bintray.com/bintray/jcenter

## Build and Contribute

Requires [Docker Compose installed](https://docs.docker.com/compose/install)

You can build the project, generate the documentation and install it in your local repository
typing:

    git clone https://github.com/jaguililla/hexagon.git
    cd hexagon
    docker-compose up -d
    docker exec hexagon_mongodb_1 mongo /benchmark.js
    ./gradle/wrapper clean site publishLocal

The results are located in the `/build` directory. And the site in `/build/site`.

For more details about Hexagon's development. Read the [contribute] section.

Code coverage grid:

![coverage](https://codecov.io/gh/jaguililla/hexagon/branch/master/graphs/tree.svg)

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
