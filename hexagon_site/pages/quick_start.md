
# What is Hexagon

Hexagon is a microservices library written in [Kotlin] which pursose is to ease the building of
services (Web applications, APIs or queue consumers) that run inside cloud platforms.

The project is developed as a [library][frameworks] that you call as oposed to [frameworks] that 
call your code inside them. Being a library means that you won't need special build settings or
tools.

It is meant to provide abstraction from underlying technoligies (data storage, HTTP server 
engines, etc.) to be able to change them with minimum impact.

It only supports [Kotlin], Java is not a targeted language for the framework.

[Kotlin]: http://kotlinlang.org
[frameworks]: https://www.quora.com/Whats-the-difference-between-a-library-and-a-framework

# Feature Comparison

Comparison with Micro Web Frameworks in Java or [Kotlin]. [Spring] and [Vert.x] are left aside as
they are full blown frameworks.

You can find a performance comparison in the [TechEmpower Web Frameworks Bechmark][benchmark]. 

|                 | [Hexagon]      | [Ktor] | [http4k] | [Spark] | [Jooby] | [Ratpack]
|-----------------|----------------|--------|----------|---------|---------|----------
|Annotated Routes |NO                |        |          |         |         |
|DSL Routes       |**YES**           |        |          |         |         |
|Data Stores      |**MongoDB**       |        |          |         |         |
|Message Queues   |**RabbitMQ**      |        |          |         |         |
|Templates        |**Pebble, Rocker**|        |          |         |         |
|CORS             |*Planned*         |        |          |         |         |
|HTTP/2           |*Planned*         |        |          |         |         |
|WebSockets       |*Planned*         |        |          |         |         |
|Configuration    |**Env, File...**  |        |          |         |         |
|Serialization    |**JSON, YAML**    |        |          |         |         |
|API Documentation|*RAML*            |        |          |         |         |
|Security         |*JWT*             |        |          |         |         |
|Non blocking I/O |*Planned*         |        |          |         |         |

[Spring]: https://spring.io
[Vert.x]: http://vertx.io
[benchmark]: https://www.techempower.com/benchmarks

[Hexagon]: http://hexagonkt.com
[Ktor]: http://ktor.io
[http4k]: http://http4k.org
[Spark]: http://sparkjava.com
[Jooby]: http://jooby.org
[Ratpack]: http://ratpack.io

# Middleware definition

TODO Mounting routers you can accomplish this

# Project Structure

The Hexagon is a multiple module project. There are several kind of modules:

* The ones that provide a single functionality (which doesn't depend on different implementations).
  Like Scheduling or Core.
* Modules that define a "Port": An interface to use a feature that may have different 
  implementations.
* "Ports", which are ports implementations for a given tool.
* Infrastructure modules. Components used by the project itself, like the benchmark, the examples
  and the site generator.

# Starters

# Gradle utility scripts

# Examples

# Why it was created

* More time reading framework docs than coding
* Frameworks targeted to everyone instead my custom needs
* Easy to hack better than work ok for every use case
* For fun!
* To learn Kotlin
* Managed services of public clouds (cheaper, but tie you to them)

# How it works

The framework is build upon smaller pieces:

## Modules

ports and adapters

# Concepts

Service (API, Web, Consumer)

Routes

Filters

Handlers

Callbacks

Routers

Servers

Templates

Events

... Stores + Rest

# Roadmap

Async

Registering

Health checks

Tool for client requests

CBOR

