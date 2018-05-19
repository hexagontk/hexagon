
Alternatives
============

# Feature Comparison

Comparison with Micro Web Frameworks in Java or [Kotlin]. [Spring] and [Vert.x] are left aside as
they are full blown frameworks.

You can find a performance comparison in the [TechEmpower Web Frameworks Bechmark][benchmark]. 

|                 | [Hexagon]           | [Ktor] | [http4k] | [Spark] | [Jooby] | [Ratpack]
|-----------------|---------------------|--------|----------|---------|---------|----------
|HTTP Engines     |**Jetty, Servlet**   |        |          |         |      
   |
|DSL Routes       |**YES**              |        |          |         |         |
|Annotated Routes |NO                   |        |          |         |         |
|CORS             |*Planned*            |        |          |         |         |
|HTTP/2           |*Planned*            |        |          |         |         |
|WebSockets       |*Planned*            |        |          |         |         |
|Non blocking I/O |*Planned*            |        |          |         |         |
|Serialization    |**JSON, YAML**       |        |          |         |         |
|Templates        |**Pebble, Rocker**   |        |          |         |         |
|Security         |*JWT*                |        |          |         |         |
|API Documentation|*RAML*               |        |          |         |         |
|Data Stores      |**MongoDB**          |        |          |         |         |
|Message Queues   |**RabbitMQ**         |        |          |         |         |
|Configuration    |**Env, File, URL...**|        |          |         |         |

[Kotlin]: http://kotlinlang.org

[Spring]: https://spring.io
[Vert.x]: http://vertx.io
[benchmark]: https://www.techempower.com/benchmarks

[Hexagon]: http://hexagonkt.com
[Ktor]: http://ktor.io
[http4k]: http://http4k.org
[Spark]: http://sparkjava.com
[Jooby]: http://jooby.org
[Ratpack]: http://ratpack.io
