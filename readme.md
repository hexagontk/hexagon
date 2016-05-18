[![BuildImg]][Build] [![CoverageImg]][Coverage]
[![DownloadImg]][Download] [![KanbanImg]][Kanban]
[![WebImg]][Web]

[BuildImg]: https://travis-ci.org/jaguililla/hexagon.svg?branch=master
[Build]: https://travis-ci.org/jaguililla/hexagon

[CoverageImg]: https://codecov.io/gh/jaguililla/hexagon/branch/master/graph/badge.svg
[Coverage]: https://codecov.io/gh/jaguililla/hexagon

[DownloadImg]: https://img.shields.io/bintray/v/jamming/maven/Hexagon.svg
[Download]: https://bintray.com/jamming/maven/Hexagon/_latestVersion

[KanbanImg]: https://img.shields.io/badge/kanban-huboard-blue.svg
[Kanban]: https://huboard.com/jaguililla/hexagon

[WebImg]: https://img.shields.io/badge/web-there4.co%2Fhexagon-blue.svg
[Web]: http://there4.co/hexagon

HEXAGON
=======
### The atoms of your platform

Hexagon is a micro services framework that doesn't follow the flock. It is written in [Kotlin] and
uses [Ratpack], [Jackson], [RabbitMQ] and [MongoDB]. It takes care of:

* [rest](http://there4.co/hexagon/rest.html)
* [messaging](http://there4.co/hexagon/messaging.html) (TODO Write documentation)
* [serialization](http://there4.co/hexagon/serialization.html) (TODO Write documentation)
* [storage](http://there4.co/hexagon/storage.html) (TODO Write documentation)
* [events](http://there4.co/hexagon/events.html)
* [configuration](http://there4.co/hexagon/configuration.html) (TODO Not implemented)
* [templates](http://there4.co/hexagon/templates.html) (TODO Partial implementation)
* [scheduling](http://there4.co/hexagon/scheduling.html)
* [testing](http://there4.co/hexagon/testing.html) (TODO Write documentation)

The purpose of the project is to provide a micro services framework with the following priorities
(in order):

1. Simple to use
2. Easily hackable
3. Be small

The name and logo are an hexagon because it is the usual way of representing a microservice in a
diagram.

DISCLAIMER: The project status right now is beta. Use it at your own risk

[Kotlin]: http://kotlinlang.org
[Ratpack]: http://ratpack.io
[Jackson]: http://wiki.fasterxml.com/JacksonHome
[RabbitMQ]: http://www.rabbitmq.com
[MongoDB]: https://www.mongodb.com

## Getting started

Get the dependency from [JCenter][JCenter] (you need to setup de repository first):

Gradle:

```groovy
compile ('co.there4:hexagon:${version}')
```

Maven:

```xml
<dependency>
  <groupId>co.there4</groupId>
  <artifactId>hexagon</artifactId>
  <version>${version}</version>
</dependency>
```

[JCenter]: https://bintray.com/jamming/maven/Hexagon

Write the code:

```java
import co.there4.hexagon.rest.*

fun main(args: Array<String>) {
    applicationStart {
        handlers {
            get("hello/:name") { ok("Hello ${pathTokens["name"]}!") }
        }
    }
}
```

Launch it and view the results at: [http://localhost:5050/hello]


## Build and Contribute

Requires [Docker Compose installed](https://docs.docker.com/compose/install)

You can build the project, generate the documentation and install it in your local repository
typing:

    git clone https://github.com/jaguililla/hexagon.git
    cd hexagon
    docker-compose -f src/test/services.yml up -d
    ./gradle/wrapper --no-daemon clean docs site publishToMavenLocal

The results are located in the `/build` directory

Code coverage grid:

![coverage](https://codecov.io/gh/jaguililla/hexagon/branch/master/graphs/tree.svg)


LICENSE
-------

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
