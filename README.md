
<h3 align="center">
  <a href="https://hexagonkt.com">
    <img alt="Hexagon" src="hexagon_site/assets/tile-small.png" />
  </a>
  <br>
  Hexagon
</h3>

<h4 align="center">The atoms of your platform</h4>

<p align="center">
  <a href="https://github.com/hexagonkt/hexagon/actions">
    <img
      alt="GitHub Actions"
      src=
        "https://github.com/hexagonkt/hexagon/workflows/.github/workflows/main.yml/badge.svg?branch=master&event=push" />
  </a>
  <a href="https://sonarcloud.io/dashboard?id=hexagonkt_hexagon">
    <img
      src=
       "https://sonarcloud.io/api/project_badges/measure?project=hexagonkt_hexagon&metric=alert_status"
      alt="SonarQube" />
  </a>
  <a href="https://bintray.com/hexagonkt/hexagon/hexagon_core/_latestVersion">
    <img
      src="https://api.bintray.com/packages/hexagonkt/hexagon/hexagon_core/images/download.svg"
      alt="Bintray" />
  </a>
</p>

<p align="center">
  <a href="https://hexagonkt.com/index.html">Home Site</a> |
  <a href="https://hexagonkt.com/quick_start/index.html">Quick Start</a> |
  <a href="https://hexagonkt.com/developer_guide/index.html">Developer Guide</a> |
  <a href="https://hexagonkt.com/port_http_server/index.html">HTTP Server</a>
</p>

---

## What is Hexagon

Hexagon is a microservices toolkit (not a [framework]) written in [Kotlin]. Its purpose is to ease
the building of services (Web applications, APIs or queue consumers) that run inside a cloud
platform.

It is meant to provide abstraction from underlying technologies (data storage, HTTP server engines,
etc.) to be able to change them with minimum impact. It is designed to fit in applications that
conforms to the [Hexagonal Architecture] (also called [Clean Architecture] or
[Ports and Adapters Architecture]).

The goals of the project are:

1. Be simple to use: make it easy to develop user services (HTTP or message consumers) quickly. It
   is focused on making the usual tasks easy.
2. Make it easy to hack: allow the user to add extensions or change the toolkit itself. The code is
   meant to be simple for the users to understand it.

Which are NOT project goals:

1. To be the fastest framework. Write the code fast and optimize only the critical parts. It is
   [not slow][benchmark] anyway.
2. Support all available technologies and tools: the spirit is to define simple interfaces for
   the most common features , so users can implement integrations with different tools easily.
3. To be usable from Java. Hexagon is *Kotlin first*.

[Kotlin]: http://kotlinlang.org
[framework]: https://stackoverflow.com/a/3057818/973418
[Hexagonal Architecture]: http://fideloper.com/hexagonal-architecture
[Clean Architecture]: https://8thlight.com/blog/uncle-bob/2012/08/13/the-clean-architecture.html
[Ports and Adapters Architecture]: https://herbertograca.com/2017/09/14/ports-adapters-architecture
[benchmark]: https://www.techempower.com/benchmarks

## Hexagon Structure

There are three kinds of client libraries:

* The ones that provide a single functionality that does not depend on different implementations.
  These modules can depend on any Ports, but never on Adapters (see below).
* Modules that define a "Port": An interface to a feature that may have different implementations.
  Ports are independent of each other.
* Adapter modules, which are Port implementations for a given tool.

For more information, take a look at the [Developer Guide].

## Simple HTTP service

You can clone a starter project ([Gradle Starter] or [Maven Starter]). Or you can create a project
from scratch following these steps:

1. Configure [Kotlin] in [Gradle][Setup Gradle] or [Maven][Setup Maven].
2. Setup the [JCenter] and [Hexagon] repositories (follow the links and click on the `Set me up!`
   button).
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
package com.hexagonkt.starters

import com.hexagonkt.helpers.logger
import com.hexagonkt.http.httpDate
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import com.hexagonkt.injection.InjectionManager

val injector = InjectionManager.apply {
    bindObject<ServerPort>(JettyServletAdapter()) // Bind Jetty server to HTTP Server Port
}

/**
 * Service server. Adapter is injected.
 */
val server: Server = Server {
    before {
        response.headers["Date"] = httpDate()
    }

    get("/hello/{name}") {
        ok("Hello, ${pathParameters["name"]}!", "text/plain")
    }
}

/**
 * Start the service from the command line.
 */
fun main() {
    logger.info { injector }
    server.start()
}
// hello
```

5. Run the service and view the results at: [http://localhost:2010/hello/world][Endpoint]

You can check the [Developer Guide] for more details. Or you can clone the [Gradle Starter] or
[Maven Starter] for a minimal fully working example (including tests).

[Gradle Starter]: https://github.com/hexagonkt/gradle_starter
[Maven Starter]: https://github.com/hexagonkt/maven_starter
[Setup Gradle]: https://kotlinlang.org/docs/reference/using-gradle.html
[Setup Maven]: https://kotlinlang.org/docs/reference/using-maven.html
[JCenter]: https://bintray.com/bintray/jcenter
[Hexagon]: https://bintray.com/hexagonkt/hexagon
[Endpoint]: http://localhost:2010/hello/world
[Developer Guide]: http://hexagonkt.com/developer_guide/index.html

## Examples

<details>
<summary>Books Example</summary>

A simple CRUD example showing how to manage book resources. Here you can check the
[full test](port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/BooksTest.kt).

```kotlin
// books
data class Book(val author: String, val title: String)

private val books: MutableMap<Int, Book> = linkedMapOf(
    100 to Book("Miguel de Cervantes", "Don Quixote"),
    101 to Book("William Shakespeare", "Hamlet"),
    102 to Book("Homer", "The Odyssey")
)

val server: Server = Server(adapter) {
    post("/books") {
        // Require fails if parameter does not exists
        val author = queryParameters.require("author")
        val title = queryParameters.require("title")
        val id = (books.keys.max() ?: 0) + 1
        books += id to Book(author, title)
        send(201, id)
    }

    get("/books/{id}") {
        val bookId = pathParameters.require("id").toInt()
        val book = books[bookId]
        if (book != null)
            // ok() is a shortcut to send(200)
            ok("Title: ${book.title}, Author: ${book.author}")
        else
            send(404, "Book not found")
    }

    put("/books/{id}") {
        val bookId = pathParameters.require("id").toInt()
        val book = books[bookId]
        if (book != null) {
            books += bookId to book.copy(
                author = queryParameters["author"] ?: book.author,
                title = queryParameters["title"] ?: book.title
            )

            ok("Book with id '$bookId' updated")
        }
        else {
            send(404, "Book not found")
        }
    }

    delete("/books/{id}") {
        val bookId = pathParameters.require("id").toInt()
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
// books
```
</details>

<details>
<summary>Session Example</summary>

Example showing how to use sessions. Here you can check the
[full test](port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/SessionTest.kt).

```kotlin
// session
val server: Server = Server(adapter) {
    path("/session") {
        get("/id") { ok(session.id ?: "null") }
        get("/access") { ok(session.lastAccessedTime?.toString() ?: "null") }
        get("/new") { ok(session.isNew()) }

        path("/inactive") {
            get { ok(session.maxInactiveInterval ?: "null") }

            put("/{time}") {
                session.maxInactiveInterval = pathParameters.require("time").toInt()
            }
        }

        get("/creation") { ok(session.creationTime ?: "null") }
        post("/invalidate") { session.invalidate() }

        path("/{key}") {
            put("/{value}") {
                session.set(pathParameters.require("key"), pathParameters.require("value"))
            }

            get { ok(session.get(pathParameters.require("key")).toString()) }
            delete { session.remove(pathParameters.require("key")) }
        }

        get {
            val attributes = session.attributes
            val attributeTexts = attributes.entries.map { it.key + " : " + it.value }

            response.headers["attributes"] = attributeTexts.joinToString(", ")
            response.headers["attribute values"] = attributes.values.joinToString(", ")
            response.headers["attribute names"] = attributes.keys.joinToString(", ")

            response.headers["creation"] = session.creationTime.toString()
            response.headers["id"] = session.id ?: ""
            response.headers["last access"] = session.lastAccessedTime.toString()

            response.status = 200
        }
    }
}
// session
```
</details>

<details>
<summary>Cookies Example</summary>

Demo server to show the use of cookies. Here you can check the
[full test](port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/CookiesTest.kt).

```kotlin
// cookies
val server: Server = Server(adapter) {
    post("/assertNoCookies") {
        if (request.cookies.isNotEmpty())
            halt(500)
    }

    post("/addCookie") {
        val name = queryParameters["cookieName"]
        val value = queryParameters["cookieValue"]
        response.addCookie(HttpCookie(name, value))
    }

    post("/assertHasCookie") {
        val cookieName = queryParameters.require("cookieName")
        val cookieValue = request.cookies[cookieName]?.value
        if (queryParameters["cookieValue"] != cookieValue)
            halt(500)
    }

    post("/removeCookie") {
        response.removeCookie(queryParameters.require("cookieName"))
    }
}
// cookies
```
</details>

<details>
<summary>Error Handling Example</summary>

Code to show how to handle callback exceptions and HTTP error codes. Here you can check the
[full test](port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/ErrorsTest.kt).

```kotlin
// errors
class CustomException : IllegalArgumentException()

val server: Server = Server(adapter) {
    error(UnsupportedOperationException::class) {
        response.headers["error"] = it.message ?: it.javaClass.name
        send(599, "Unsupported")
    }

    error(IllegalArgumentException::class) {
        response.headers["runtimeError"] = it.message ?: it.javaClass.name
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
// errors
```
</details>

<details>
<summary>Filters Example</summary>

This example shows how to add filters before and after route execution. Here you can check the
[full test](port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/FiltersTest.kt).

```kotlin
// filters
private val users: Map<String, String> = mapOf(
    "Turing" to "London",
    "Dijkstra" to "Rotterdam"
)

private val server: Server = Server(adapter) {
    before { attributes["start"] = nanoTime() }

    before("/protected/*") {
        val authorization = request.headers["Authorization"] ?: halt(401, "Unauthorized")
        val credentials = authorization.removePrefix("Basic ")
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
    after { response.headers["time"] = nanoTime() - attributes["start"] as Long }
}
// filters
```
</details>

<details>
<summary>Files Example</summary>

The following code shows how to serve resources and receive files. Here you can check the
[full test](https://github.com/hexagonkt/hexagon/blob/master/port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/FilesTest.kt).

```kotlin
// files
private val server: Server = Server(adapter) {
    path("/static") {
        get("/files/*", Resource("assets")) // Serve `assets` resources on `/html/*`
        get("/resources/*", File(directory)) // Serve `test` folder on `/pub/*`
    }

    get("/html/*", Resource("assets")) // Serve `assets` resources on `/html/*`
    get("/pub/*", File(directory)) // Serve `test` folder on `/pub/*`
    get(Resource("public")) // Serve `public` resources folder on `/*`

    post("/multipart") { ok(request.parts.keys.joinToString(":")) }

    post("/file") {
        val part = request.parts.values.first()
        val content = part.inputStream.reader().readText()
        ok(content)
    }

    post("/form") {
        fun serializeMap(map: Map<String, List<String>>): List<String> = listOf(
            map.map { "${it.key}:${it.value.joinToString(",")}}" }.joinToString("\n")
        )

        val queryParams = serializeMap(queryParametersValues)
        val formParams = serializeMap(formParametersValues)

        response.headersValues["queryParams"] = queryParams
        response.headersValues["formParams"] = formParams
    }
}
// files
```
</details>

<details>
<summary>CORS Example</summary>

The following code shows how to set up CORS for REST APIs used from the browser. You can check the
[full test](https://github.com/hexagonkt/hexagon/blob/master/port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/CorsTest.kt).

```kotlin
// cors
val server: Server = Server(adapter) {
    corsPath("/default", CorsSettings())
    corsPath("/example/org", CorsSettings("example.org"))
    corsPath("/no/credentials", CorsSettings(supportCredentials = false))
    corsPath("/only/post", CorsSettings(allowedMethods = setOf(POST)))
    corsPath("/cache", CorsSettings(preFlightMaxAge = 10))
    corsPath("/exposed/headers", CorsSettings(exposedHeaders = setOf("head")))
    corsPath("/allowed/headers", CorsSettings(allowedHeaders = setOf("head")))
}

private fun Router.corsPath(path: String, settings: CorsSettings) {
    path(path) {
        // CORS settings can change for different routes
        cors(settings)

        get("/path") { ok(request.method) }
        post("/path") { ok(request.method) }
        put("/path") { ok(request.method) }
        delete("/path") { ok(request.method) }
        get { ok(request.method) }
        post { ok(request.method) }
        put { ok(request.method) }
        delete { ok(request.method) }
    }
}
// cors
```
</details>

<details>
<summary>HTTPS Example</summary>

The snippet below shows how to set up your server to use HTTPS and HTTP/2. You can check the
[full test](https://github.com/hexagonkt/hexagon/blob/master/port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/HttpsTest.kt).

```kotlin
// https
// Key store files
val identity = "hexagonkt.p12"
val trust = "trust.p12"

// Default passwords are file name reversed
val keyStorePassword = identity.reversed()
val trustStorePassword = trust.reversed()

// Key stores can be set as URIs to classpath resources (the triple slash is needed)
val keyStore = URI("resource:///ssl/$identity")
val trustStore = URI("resource:///ssl/$trust")

val sslSettings = SslSettings(
    keyStore = keyStore,
    keyStorePassword = keyStorePassword,
    trustStore = trustStore,
    trustStorePassword = trustStorePassword,
    clientAuth = true // Requires a valid certificate from the client (mutual TLS)
)

val serverSettings = ServerSettings(
    bindPort = 0,
    protocol = HTTPS, // You can also use HTTP2
    sslSettings = sslSettings
)

val server = serve(serverSettings, serverAdapter) {
    get("/hello") {
        // We can access the certificate used by the client from the request
        val subjectDn = request.certificate?.subjectDN?.name
        response.headers["cert"] = subjectDn
        ok("Hello World!")
    }
}

// We'll use the same certificate for the client (in a real scenario it would be different)
val clientSettings = ClientSettings(sslSettings = sslSettings)

// Create a HTTP client and make a HTTPS request
val client = Client(AhcAdapter(), "https://localhost:${server.runtimePort}", clientSettings)
client.get("/hello").apply {
    logger.debug { body }
    // Assure the certificate received (and returned) by the server is correct
    assert(headers.require("cert").first().startsWith("CN=hexagonkt.com"))
    assert(body == "Hello World!")
}
// https
```
</details>

## Status

**DISCLAIMER**: The project is not yet production ready. Use it at your own risk.

Performance is not the primary goal, but it is taken seriously. You can check performance numbers
in the [TechEmpower Web Framework Benchmarks][benchmark]. You can also run the stress tests, to do
so, read the [Benchmark readme](hexagon_benchmark/README.md).

Tests, of course, are taken into account. These are some coverage metrics:

[![Coverage]][SonarCoverage] [![Lines]][SonarLines]

[Coverage]: https://sonarcloud.io/api/project_badges/measure?project=hexagonkt_hexagon&metric=coverage
[Lines]: https://sonarcloud.io/api/project_badges/measure?project=hexagonkt_hexagon&metric=ncloc

The code quality is checked by [SonarQube]:

[![Maintainability]][SonarMaintainability] [![Reliability]][SonarReliability] [![Security]][SonarSecurity]

[Maintainability]: https://sonarcloud.io/api/project_badges/measure?project=hexagonkt_hexagon&metric=sqale_rating
[Reliability]: https://sonarcloud.io/api/project_badges/measure?project=hexagonkt_hexagon&metric=reliability_rating
[Security]: https://sonarcloud.io/api/project_badges/measure?project=hexagonkt_hexagon&metric=security_rating

[SonarCoverage]: https://sonarcloud.io/component_measures?id=hexagonkt_hexagon&metric=coverage&view=list
[SonarLines]: https://sonarcloud.io/component_measures?id=hexagonkt_hexagon&metric=ncloc&view=list

[SonarMaintainability]: https://sonarcloud.io/component_measures?id=hexagonkt_hexagon&metric=Maintainability
[SonarReliability]: https://sonarcloud.io/component_measures?id=hexagonkt_hexagon&metric=Reliability
[SonarSecurity]: https://sonarcloud.io/component_measures?id=hexagonkt_hexagon&metric=Security

[SonarQube]: https://sonarcloud.io

## Contribute

If you like this project and want to support it, the easiest way is to [give it a star] :v:.

If you feel like you can do more. You can contribute to the project in different ways:

* By using it and [spreading the word][@hexagon_kt].
* Giving feedback by [Twitter][@hexagon_kt] or [Slack].
* Requesting [new features or submitting bugs][issues].
* Voting for the features you want in the [issue tracker][issues] (using [reactions]).
* And... Drum roll... Submitting [code or documentation][contributing].

To know what issues are currently open and be aware of the next features you can check the
[Project Board] at GitHub.

You can ask any question, suggestion or complaint at the project's [Slack channel][Slack]. You can
be up to date of project's news following [@hexagon_kt] on Twitter.

Thanks to all project's [contributors]!

[![CodeTriage](https://www.codetriage.com/hexagonkt/hexagon/badges/users.svg)][CodeTriage]

[give it a star]: https://github.com/hexagonkt/hexagon/stargazers
[@hexagon_kt]: https://twitter.com/hexagon_kt
[Slack]: https://kotlinlang.slack.com/messages/hexagon
[issues]: https://github.com/hexagonkt/hexagon/issues
[reactions]: https://github.com/blog/2119-add-reactions-to-pull-requests-issues-and-comments
[contributing]: contributing.md
[Project Board]: https://github.com/hexagonkt/hexagon/projects/1
[contributors]: https://github.com/hexagonkt/hexagon/graphs/contributors
[CodeTriage]: https://www.codetriage.com/hexagonkt/hexagon

## License

The project is licensed under the [MIT License]. This license lets you use the source for free or
commercial purposes as long as you provide attribution and don’t hold any project member liable.

[MIT License]: license.md
