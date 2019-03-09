
# What is Hexagon

Hexagon is a microservices toolkit written in [Kotlin]. Its purpose is to ease the building of
services (Web applications, APIs or queue consumers) that run inside a cloud platform.

The project is developed as a set of [libraries][frameworks] that you call as opposed to
[frameworks] that call your code inside them. Being a library means that you won't need special
build settings or tools.

It is meant to provide abstraction from underlying technologies (data storage, HTTP server engines,
etc.)to be able to change them with minimum impact. It is designed to fit in applications that
conforms to the [Hexagonal Architecture] (also called [Clean Architecture] or
[Ports and Adapters Architecture]).

The motivation that drove the development of this library was that sometimes you spend more time
reading framework's documentation than coding. And also, because you lose control of your program:
you do not call framework's code, framework's code calls you.

The goals of the project are:

1. Be simple to use: make it easy to develop user services (HTTP or message consumers) quickly. It
   is focused on making the usual tasks easy, rather than making a complex tool with a lot of
   features.
2. Make it easy to hack: allow the user to add extensions or change the framework itself. The code
   is meant to be simple for the users to understand it. Avoid having to read blogs, documentation
   or getting certified to use it effectively.

Which are NOT project goals:

1. To be the fastest framework. Write the code fast and optimize only the critical parts. It is
   [not slow][benchmark] anyway.
2. Support all available technologies and tools: the spirit is to define simple interfaces for
   the most common features , so users can implement integrations with different tools easily.
3. To be usable from Java. Hexagon is *Kotlin first*.

[Kotlin]: http://kotlinlang.org
[frameworks]: https://www.quora.com/Whats-the-difference-between-a-library-and-a-framework
[Hexagonal Architecture]: http://fideloper.com/hexagonal-architecture
[Clean Architecture]: https://8thlight.com/blog/uncle-bob/2012/08/13/the-clean-architecture.html
[Ports and Adapters Architecture]: https://herbertograca.com/2017/09/14/ports-adapters-architecture
[benchmark]: https://www.techempower.com/benchmarks

# Hexagon Libraries

Project's modules fall into two categories: the ones for internal use, and the ones that provide a
functionality that clients can import. There are three kind of client libraries:

* The ones that provide a single functionality that does not depend on different implementations,
  like [hexagon_scheduler] or [hexagon_core].
* Modules that define a "Port": An interface to a feature that may have different implementations
  (ie: [port_http_server] or [port_store]). These ones can not be used by themselves and in their
  place, an adapter implementing them should be added to the list of dependencies.
* Adapter modules, which are Port implementations for a given tool. [store_mongodb] and
  [messaging_rabbitmq] are examples of this type of modules.
  
Ports are independent from each other.

[hexagon_scheduler]: https://hexagonkt.com/hexagon_scheduler/index.html
[hexagon_core]: https://hexagonkt.com/hexagon_core/index.html

[port_http_server]: https://hexagonkt.com/port_http_server/index.html
[port_store]: https://hexagonkt.com/port_store/index.html

[store_mongodb]: https://hexagonkt.com/store_mongodb/index.html
[messaging_rabbitmq]: https://hexagonkt.com/messaging_rabbitmq/index.html

# Hexagon Core

Hexagon Core module is used by all other libraries, so it would be added to your project anyway just
by using any adapter.

The main features it has are:

* [Helpers]: JVM information, a logger and other useful utilities.
* [Dependency Injection]: bind classes to creation closures or instances and inject them.
* [Instance Serialization]: parse/serialize data in different formats to class instances.
* [Configuration Settings]: load settings from different data sources and formats.

[Helpers]: /hexagon_core/index.html#helpers
[Dependency Injection]: /hexagon_core/index.html#dependency-injection
[Instance Serialization]: /hexagon_core/index.html#instance-serialization
[Configuration Settings]: /hexagon_core/index.html#configuration-settings

# Simple HTTP service

You can clone a starter project ([Gradle Starter] or [Maven Starter]). Or you can create a project
from scratch following these steps:

1. Configure [Kotlin] in [Gradle][Setup Gradle] or [Maven][Setup Maven].
2. Setup the [JCenter] repository (follow the link and click on the `Set me up!` button).
3. Add the dependency:

  * In Gradle. Import it inside `build.gradle`:

    ```groovy
    compile ("com.hexagonkt:http_server_jetty:$hexagonVersion")
    ```

  * In Maven. Declare the dependency in `pom.xml`:

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>http_server_jetty</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

4. Write the code in the `src/main/kotlin/Hello.kt` file:

@sample hexagon_starters/src/main/kotlin/Service.kt

5. Run the service and view the results at: [http://localhost:2010/hello/world][Endpoint]

You can check the [documentation] for more details. Or you can clone the [Gradle Starter] or
[Maven Starter] for a minimal fully working example (including tests).

[Gradle Starter]: https://github.com/hexagonkt/gradle_starter
[Maven Starter]: https://github.com/hexagonkt/maven_starter
[Setup Gradle]: https://kotlinlang.org/docs/reference/using-gradle.html
[Setup Maven]: https://kotlinlang.org/docs/reference/using-maven.html
[JCenter]: https://bintray.com/bintray/jcenter
[Endpoint]: http://localhost:2010/hello/world
[documentation]: /documentation.html

# Books Example

A simple CRUD example showing how to manage book resources.

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/BooksTest.kt:books

# Session Example

Example showing how to use sessions.

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/SessionTest.kt:session

# Cookies Example

Demo server to show the use of cookies.

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/CookiesTest.kt:cookies

# Error Handling Example

Code to show how to handle callback exceptions and HTTP error codes.

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/ErrorsTest.kt:errors
