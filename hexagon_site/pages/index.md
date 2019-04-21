---
hero: |
  <p align="center">
    <img alt="Hexagon" src="tile-small.png" />
    <br />
    <a href="https://travis-ci.org/hexagonkt/hexagon">
      <img src="https://travis-ci.org/hexagonkt/hexagon.svg?branch=master" alt="Travis CI" />
    </a>
    <a href="https://codecov.io/gh/hexagonkt/hexagon">
      <img
        src="https://codecov.io/gh/hexagonkt/hexagon/branch/master/graph/badge.svg"
        alt="Codecov" />
    </a>
    <a href="https://codebeat.co/projects/github-com-hexagonkt-hexagon-master">
      <img
        src="https://codebeat.co/badges/f8fafe6f-767a-4248-bc34-e6d4a2acb971"
        alt="Codebeat" />
    </a>
    <a href="https://bintray.com/jamming/maven/hexagon_core/_latestVersion">
      <img
        src="https://api.bintray.com/packages/jamming/maven/hexagon_core/images/download.svg"
        alt="Bintray" />
    </a>
  </p>
  
  <h1 align="center">The atoms of your platform</h1>
  
  <p align="center">
    Hexagon is a microservices toolkit written in Kotlin. Its purpose is to ease the building of
    services (Web applications, APIs or queue consumers) that run inside a cloud platform
  </p>
---

# What is Hexagon

Hexagon is a microservices toolkit (not a [framework]) written in [Kotlin]. Its purpose is to ease
the building of services (Web applications, APIs or queue consumers) that run inside a cloud
platform.

It is meant to provide abstraction from underlying technologies (data storage, HTTP server engines,
etc.) to be able to change them with minimum impact. It is designed to fit in applications that
conforms to the [Hexagonal Architecture] (also called [Clean Architecture] or
[Ports and Adapters Architecture]).

The goals of the project are:

1. Be simple to use: make it easy to develop user services (HTTP or message consumers) quickly. It
   is focused on making the usual tasks easy, rather than making a complex tool with a lot of
   features.
2. Make it easy to hack: allow the user to add extensions or change the toolkit itself. The code is
   meant to be simple for the users to understand it. Avoid having to read blogs, documentation or
   getting certified to use it effectively.

Which are NOT project goals:

1. To be the fastest framework. Write the code fast and optimize only the critical parts. It is
   [not slow][benchmark] anyway.
2. Support all available technologies and tools: the spirit is to define simple interfaces for
   the most common features , so users can implement integrations with different tools easily.
3. To be usable from Java. Hexagon is *Kotlin first*.

[Kotlin]: http://kotlinlang.org
[framework]: https://www.quora.com/Whats-the-difference-between-a-library-and-a-framework
[Hexagonal Architecture]: http://fideloper.com/hexagonal-architecture
[Clean Architecture]: https://8thlight.com/blog/uncle-bob/2012/08/13/the-clean-architecture.html
[Ports and Adapters Architecture]: https://herbertograca.com/2017/09/14/ports-adapters-architecture
[benchmark]: https://www.techempower.com/benchmarks

# Hexagon Structure

There are three kind of client libraries:

* The ones that provide a single functionality that does not depend on different implementations.
* Modules that define a "Port": An interface to a feature that may have different implementations.
* Adapter modules, which are Port implementations for a given tool.
  
Ports are independent from each other.

Hexagon Core module provides convenience utilities. The main features it has are:

* [Helpers]: JVM information, a logger and other useful utilities.
* [Dependency Injection]: bind classes to creation closures or instances and inject them.
* [Instance Serialization]: parse/serialize data in different formats to class instances.
* [Configuration Settings]: load settings from different data sources and formats.

[Helpers]: /hexagon_core/com.hexagonkt.helpers
[Dependency Injection]: /hexagon_core/com.hexagonkt.injection
[Instance Serialization]: /hexagon_core/com.hexagonkt.serialization
[Configuration Settings]: /hexagon_core/com.hexagonkt.settings

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

```kotlin
// hello
import com.hexagonkt.http.httpDate
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import com.hexagonkt.injection.InjectionManager.bindObject

/**
 * Service server. It is created lazily to allow ServerPort injection (set up in main).
 */
val server: Server by lazy {
    Server {
        before {
            response.setHeader("Date", httpDate())
        }

        get("/hello/{name}") { ok("Hello, ${pathParameters["name"]}!", "text/plain") }
    }
}

/**
 * Start the service from the command line.
 */
fun main() {
    bindObject<ServerPort>(JettyServletAdapter()) // Bind Jetty server to HTTP Server Port
    server.start()
}
// hello
```

5. Run the service and view the results at: [http://localhost:2010/hello/world][Endpoint]

You can check the [documentation] for more details. Or you can clone the [Gradle Starter] or
[Maven Starter] for a minimal fully working example (including tests).

[Gradle Starter]: https://github.com/hexagonkt/gradle_starter
[Maven Starter]: https://github.com/hexagonkt/maven_starter
[Setup Gradle]: https://kotlinlang.org/docs/reference/using-gradle.html
[Setup Maven]: https://kotlinlang.org/docs/reference/using-maven.html
[JCenter]: https://bintray.com/bintray/jcenter
[Endpoint]: http://localhost:2010/hello/world
[documentation]: http://hexagonkt.com/documentation.html

# Books Example

A simple CRUD example showing how to manage book resources. Here you can check the
[full test](https://github.com/hexagonkt/hexagon/blob/master/port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/BooksTest.kt).

```kotlin
// books
data class Book(val author: String, val title: String)

private val books: MutableMap<Int, Book> = linkedMapOf(
    100 to Book("Miguel de Cervantes", "Don Quixote"),
    101 to Book("William Shakespeare", "Hamlet"),
    102 to Book("Homer", "The Odyssey")
)

val server: Server by lazy {
    Server(adapter) {
        post("/books") {
            // Require fails if parameter does not exists
            val author = parameters.require("author").first()
            val title = parameters.require("title").first()
            val id = (books.keys.max() ?: 0) + 1
            books += id to Book(author, title)
            send(201, id)
        }

        get("/books/{id}") {
            // Path parameters *must* exist an error is thrown if they are not present
            val bookId = pathParameters["id"].toInt()
            val book = books[bookId]
            if (book != null)
                // ok() is a shortcut to send(200)
                ok("Title: ${book.title}, Author: ${book.author}")
            else
                send(404, "Book not found")
        }

        put("/books/{id}") {
            val bookId = pathParameters["id"].toInt()
            val book = books[bookId]
            if (book != null) {
                books += bookId to book.copy(
                    author = parameters["author"]?.first() ?: book.author,
                    title = parameters["title"]?.first() ?: book.title
                )

                ok("Book with id '$bookId' updated")
            }
            else {
                send(404, "Book not found")
            }
        }

        delete("/books/{id}") {
            val bookId = pathParameters["id"].toInt()
            val book = books[bookId]
            books -= bookId
            if (book != null)
                ok("Book with id '$bookId' deleted")
            else
                send(404, "Book not found")
        }

        // Matches path's requests with *any* HTTP method as a fallback (return 404 instead 405)
        any("/books/{id}") { send(405) }

        get("/books") { ok(books.keys.joinToString(" ", transform = Int::toString)) }
    }
}
// books
```

# Session Example

Example showing how to use sessions. Here you can check the
[full test](https://github.com/hexagonkt/hexagon/blob/master/port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/SessionTest.kt).

```kotlin
// session
val server: Server by lazy {
    Server(adapter) {
        path("/session") {
            get("/id") { ok(session.id ?: "null") }
            get("/access") { ok(session.lastAccessedTime?.toString() ?: "null") }
            get("/new") { ok(session.isNew()) }

            path("/inactive") {
                get { ok(session.maxInactiveInterval ?: "null") }
                put("/{time}") { session.maxInactiveInterval = pathParameters["time"].toInt() }
            }

            get("/creation") { ok(session.creationTime ?: "null") }
            post("/invalidate") { session.invalidate() }

            path("/{key}") {
                put("/{value}") { session.set(pathParameters["key"], pathParameters["value"]) }
                get { ok(session.get(pathParameters["key"]).toString()) }
                delete { session.remove(pathParameters["key"]) }
            }

            get {
                val attributes = session.attributes
                val attributeTexts = attributes.entries.map { it.key + " : " + it.value }

                response.setHeader("attributes", attributeTexts.joinToString(", "))
                response.setHeader("attribute values", attributes.values.joinToString(", "))
                response.setHeader("attribute names", attributes.keys.joinToString(", "))

                response.setHeader("creation", session.creationTime.toString())
                response.setHeader("id", session.id ?: "")
                response.setHeader("last access", session.lastAccessedTime.toString())

                response.status = 200
            }
        }
    }
}
// session
```

# Cookies Example

Demo server to show the use of cookies. Here you can check the
[full test](https://github.com/hexagonkt/hexagon/blob/master/port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/CookiesTest.kt).

```kotlin
// cookies
val server: Server by lazy {
    Server(adapter) {
        post("/assertNoCookies") {
            if (!request.cookies.isEmpty())
                halt(500)
        }

        post("/addCookie") {
            val name = parameters["cookieName"]?.first()
            val value = parameters["cookieValue"]?.first()
            response.addCookie(HttpCookie(name, value))
        }

        post("/assertHasCookie") {
            val cookieName = parameters.require("cookieName").first()
            val cookieValue = request.cookies[cookieName]?.value
            if (parameters["cookieValue"]?.first() != cookieValue)
                halt(500)
        }

        post("/removeCookie") {
            response.removeCookie(parameters.require("cookieName").first())
        }
    }
}
// cookies
```

# Error Handling Example

Code to show how to handle callback exceptions and HTTP error codes. Here you can check the
[full test](https://github.com/hexagonkt/hexagon/blob/master/port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/ErrorsTest.kt).

```kotlin
// errors
class CustomException : IllegalArgumentException()

val server: Server by lazy {
    Server(adapter) {
        error(UnsupportedOperationException::class) {
            response.setHeader("error", it.message ?: it.javaClass.name)
            send(599, "Unsupported")
        }

        error(IllegalArgumentException::class) {
            response.setHeader("runtimeError", it.message ?: it.javaClass.name)
            send(598, "Runtime")
        }

        // Catching `Exception` handles any unhandled exception before (it has to be the last)
        error(Exception::class) { send(500, "Root handler") }

        // It is possible to execute a handler upon a given status code before returning
        error(588) { send(578, "588 -> 578") }

        get("/exception") { throw UnsupportedOperationException("error message") }
        get("/baseException") { throw CustomException() }
        get("/unhandledException") { error("error message") }

        get("/halt") { halt("halted") }
        get("/588") { halt(588) }
    }
}
// errors
```

# Filters Example

This example shows how to add filters before and after route execution. Here you can check the
[full test](https://github.com/hexagonkt/hexagon/blob/master/port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/FiltersTest.kt).

```kotlin
// filters
private val users: Map<String, String> = mapOf(
    "Turing" to "London",
    "Dijkstra" to "Rotterdam"
)

private val server: Server by lazy {
    Server(adapter) {
        before { attributes["start"] = nanoTime() }

        before("/protected/*") {
            val authorization = request.headers["Authorization"] ?: halt(401, "Unauthorized")
            val credentials = authorization.first().removePrefix("Basic ")
            val userPassword = String(Base64.getDecoder().decode(credentials)).split(":")

            // Parameters set in call attributes are accessible in other filters and routes
            attributes["username"] = userPassword[0]
            attributes["password"] = userPassword[1]
        }

        // All matching filters are run in order unless call is halted
        before("/protected/*") {
            if(users[attributes["username"]] != attributes["password"])
                halt(403, "Forbidden")
        }

        get("/protected/hi") { ok("Hello ${attributes["username"]}!") }

        // After filters are ran even if request was halted before
        after { response.setHeader("time", nanoTime() - attributes["start"] as Long) }
    }
}
// filters
```

# Files Example

The following code shows how to serve resources and receive files. Here you can check the
[full test](https://github.com/hexagonkt/hexagon/blob/master/port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/FilesTest.kt).

```kotlin
// files
private val server: Server by lazy {
    Server(adapter) {
        assets("assets", "/html/*") // Serves `assets` resources on `/html/*`
        assets("public") // Serves `public` resources folder on `/*`
        post("/multipart") { ok(request.parts.keys.joinToString(":")) }
        post("/file") {
            val part = request.parts.values.first()
            val content = part.inputStream.reader().readText()
            ok(content)
        }
    }
}
// files
```

# Status

**DISCLAIMER**: The project is not yet production ready. Use it at your own risk. There are some
modules not finished yet (e.g: storage and HTTP client).

It is used in personal not released projects to develop APIs and Web applications.

Performance is not the primary goal, but it is taken seriously. You can check performance numbers
in the [TechEmpower Web Framework Benchmarks][benchmark]. You can also run the stress tests, to do
so, read the [Benchmark readme](https://github.com/hexagonkt/hexagon/blob/master/hexagon_benchmark/README.md)

Tests, of course, are taken into account. This is the coverage grid:

[![CoverageGrid]][Coverage]

The code quality is checked by Codebeat:

[![codebeat badge]][codebeat page]

[CoverageGrid]: https://codecov.io/gh/hexagonkt/hexagon/branch/master/graphs/icicle.svg
[Coverage]: https://codecov.io/gh/hexagonkt/hexagon
[codebeat badge]: https://codebeat.co/badges/f8fafe6f-767a-4248-bc34-e6d4a2acb971
[codebeat page]: https://codebeat.co/projects/github-com-hexagonkt-hexagon-master

# Contribute

If you like this project and want to support it, the easiest way is to [give it a star] :v:.

If you feel like you can do more. You can contribute to the project in different ways:

* By using it and [spreading the word][@hexagon_kt].
* Giving feedback by [Twitter][@hexagon_kt] or [Slack].
* Requesting [new features or submitting bugs][issues].
* Voting for the features you want in the [issue tracker][issues] (using [reactions]).
* And... Drum roll... Submitting [code or documentation][contributing].

To know what issues are currently open and be aware of the next features you can check the
[Project Board] at Github.

You can ask any question, suggestion or complaint at the project's [Slack channel][Slack]. And be up
to date of project's news following [@hexagon_kt] in Twitter.

Thanks to all project's [contributors]!

[give it a star]: https://github.com/hexagonkt/hexagon/stargazers
[@hexagon_kt]: https://twitter.com/hexagon_kt
[Slack]: https://kotlinlang.slack.com/messages/hexagon
[issues]: https://github.com/hexagonkt/hexagon/issues
[reactions]: https://github.com/blog/2119-add-reactions-to-pull-requests-issues-and-comments
[contributing]: https://github.com/hexagonkt/hexagon/blob/master/contributing.md
[Project Board]: https://github.com/hexagonkt/hexagon/projects/1
[contributors]: https://github.com/hexagonkt/hexagon/graphs/contributors

# License

The project is licensed under the [MIT License]. This license lets you use the source for free or
commercial purposes as long as you provide attribution and don’t hold any project member liable.

[MIT License]: https://github.com/hexagonkt/hexagon/blob/master/license.md
