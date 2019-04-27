
# Feature Comparison

Comparison with other Micro Web Frameworks in Java or [Kotlin].

|                 | [Hexagon]       | [Ktor]                       | [http4k]                                      | [Spark]                                                           | [Jooby]
|-----------------|-----------------|------------------------------|-----------------------------------------------|-------------------------------------------------------------------|----------------------
|HTTP Engines     |Jetty, Servlet   |Jetty, Netty, Servlet         |Jetty, Netty, Undertow, Servlet                |Jetty                                                              |Jetty, Netty, Undertow
|DSL Routes       |YES              |YES                           |YES                                            |YES                                                                |YES
|Annotated Routes |NO               |YES                           |NO                                             |WebSockets Only                                                    |YES
|CORS             |*Planned*        |YES                           |YES                                            |NO                                                                 |YES
|HTTP/2           |*Planned*        |YES                           |Servlets Only                                  |NO                                                                 |YES
|WebSockets       |*Planned*        |YES                           |YES                                            |YES                                                                |YES
|Non blocking I/O |*Planned*        |YES                           |NO                                             |NO                                                                 |YES
|Serialization    |JSON, YAML       |JSON                          |JSON, XML                                      |JSON                                                               |JSON
|Templates        |Pebble           |Freemarker, Mustache, Velocity|Freemarker, Pebble, Dust, Handlebars, Thymeleaf|Freemarker, Mustache, Velocity, Pebble, Jade, Handlebars, Thymeleaf|Freemarker, Pebble, Jade, Handlebars, Thymeleaf, Rocker
|Security         |*JWT (Planned)*  |JWT, LDAP, OAuth              |OAuth                                          |None                                                               |OAuth, CAS, SAML, OpenID Connect, LDAP, JWT ([pac4j])
|API Documentation|*RAML (Planned)* |None                          |Swagger                                        |None                                                               |Swagger, RAML
|Data Stores      |MongoDB          |None                          |None                                           |None                                                               |JDBC, Cassandra, MongoDB, Couchbase, Elasticsearch, Neo4j
|Message Queues   |RabbitMQ         |None                          |None                                           |None                                                               |None
|Configuration    |Env, File, URL   |HOCON                         |By code                                        |By code                                                            |HOCON

[Kotlin]: http://kotlinlang.org

[Hexagon]: http://hexagonkt.com
[Ktor]: http://ktor.io
[http4k]: http://http4k.org
[Spark]: http://sparkjava.com
[Jooby]: http://jooby.org
[Ratpack]: http://ratpack.io

[pac4j]: http://www.pac4j.org

# Performance Benchmark

You can find a performance comparison in the [TechEmpower Web Frameworks Benchmark][benchmark]. 

[benchmark]: https://www.techempower.com/benchmarks
