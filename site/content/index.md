title=Hexagon
date=2016-04-13
type=page
status=published
~~~~~~


HEXAGON ${projectVersion}
=========================
### The atoms of your platform

Hexagon is a micro services framework that doesn't follow the flock. It is written in [Kotlin] and
uses [RabbitMQ] and [MongoDB]. It takes care of:

* [rest](rest.html)
* [serialization](serialization.html) (TODO Write documentation)
* [storage](storage.html) (TODO Write documentation)
* [events](events.html)
* [configuration](configuration.html) (TODO Partial implementation)
* [templates](templates.html) (TODO Partial implementation)
* [scheduling](scheduling.html)
* [testing](testing.html) (TODO Write documentation)
* [builds](builds.html)

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

## Getting Started

Get the dependency from [JCenter] (you need to [setup the repository] first):

[setup the repository]: https://bintray.com/bintray/jcenter

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

```kotlin
import co.there4.hexagon.web.*

fun main(args: Array<String>) {
    get("/hello/{name}") { ok("Hello ${request["name"]}!") }
    run()
}
```

Launch it and view the results at: [http://localhost:2010/hello]

## Build and Contribute

Check the [Github's readme file](https://github.com/jaguililla/hexagon#build-and-contribute)
