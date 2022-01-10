
<h3 align="center">
  <a href="https://hexagonkt.com">
    <img alt="Hexagon" src="https://hexagonkt.com/tile-small.png" />
  </a>
  <br>
  Hexagon
</h3>

<h4 align="center">The atoms of your platform</h4>

<p align="center">
  <a href="https://github.com/hexagonkt/hexagon/actions">
    <img
      alt="GitHub Actions"
      src="https://github.com/hexagonkt/hexagon/workflows/Release/badge.svg" />
  </a>
  <a href="https://hexagonkt.com/jacoco">
    <img src="https://hexagonkt.com/img/coverage.svg" alt="Coverage" />
  </a>
  <a href="https://search.maven.org/search?q=g:com.hexagonkt">
    <img src="https://hexagonkt.com/img/download.svg" alt="Maven Central Repository" />
  </a>
</p>

<p align="center">
  <a href="https://hexagonkt.com">Home Site</a> |
  <a href="https://hexagonkt.com/quick_start">Quick Start</a> |
  <a href="https://hexagonkt.com/developer_guide">Developer Guide</a>
</p>

---

## What is Hexagon

Hexagon is a microservices' toolkit (not a [framework]) written in [Kotlin]. Its purpose is to ease
the building of server applications (Web applications, APIs or queue consumers) that run inside a
cloud platform.

The Hexagon Toolkit provides several libraries to build server applications. These libraries provide
single standalone features and are referred to as ["Ports"][Ports and Adapters Architecture].

The main ports are:

* [The HTTP server]: supports HTTPS, HTTP/2, mutual TLS, static files (serve and upload), forms
  processing, cookies, sessions, CORS and more.
* [The HTTP client]: which supports mutual TLS, HTTP/2, cookies, form fields and files among other
  features.
* [Template Processing]: allows template processing from URLs (local files, resources or HTTP
  content) binding name patterns to different engines.

Each of these features or ports may have different implementations called
["Adapters"][Ports and Adapters Architecture].

Hexagon is designed to fit in applications that conform to the [Hexagonal Architecture] (also called
[Clean Architecture] or [Ports and Adapters Architecture]). Also, its design principles also fits in
this architecture.

The Hexagon's goals and design principles are:

* **Put you in Charge**: There is no code generation, no runtime annotation processing, no classpath
  based logic, and no implicit behaviour. You control your tools, not the other way around.

* **Modular**: Each feature (Port) or adapter is isolated in its own module. Use only the modules
  you need without carrying unneeded dependencies.

* **Pluggable Adapters**: Every Port may have many implementations (Adapters) using different
  technologies. You can swap adapters without changing the application code.

* **Batteries Included**: It contains all the required pieces to make production-grade applications:
  logging utilities, serialization, resource handling and build helpers.

* **Kotlin First**: Take full advantage of Kotlin instead of just calling Java code from Kotlin. The
  library is coded in Kotlin for coding with Kotlin. No strings attached to Java (as a Language).

* **Properly Tested**: The project's coverage is checked in every Pull Request. It is also
  stress-tested at [TechEmpower Frameworks Benchmark][benchmark].

For more information check the [Quick Start Guide] or the [Developer Guide].

[framework]: https://stackoverflow.com/a/3057818/973418
[Kotlin]: http://kotlinlang.org
[The HTTP server]: http://hexagonkt.com/http_server
[The HTTP client]: http://hexagonkt.com/http_client
[Template Processing]: http://hexagonkt.com/templates
[Hexagonal Architecture]: http://fideloper.com/hexagonal-architecture
[Clean Architecture]: https://8thlight.com/blog/uncle-bob/2012/08/13/the-clean-architecture.html
[Ports and Adapters Architecture]: https://herbertograca.com/2017/09/14/ports-adapters-architecture
[Quick Start Guide]: http://hexagonkt.com/quick_start
[Developer Guide]: http://hexagonkt.com/developer_guide

## Simple HTTP service

You can clone a starter project ([Gradle Starter] or [Maven Starter]). Or you can create a project
from scratch following these steps:

1. Configure Kotlin in [Gradle][Setup Gradle] or [Maven][Setup Maven].
2. Add the dependency:

  * In Gradle. Import it inside `build.gradle`:

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagonkt:http_server_jetty:$hexagonVersion")
    ```

  * In Maven. Declare the dependency in `pom.xml`:

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>http_server_jetty</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

3. Write the code in the `src/main/kotlin/Hello.kt` file:

```kotlin
// hello_world
import com.hexagonkt.http.server.jetty.serve

lateinit var server: HttpServer

/**
 * Start a Hello World server, serving at path "/hello".
 */
fun main() {
    server = serve {
        get("/hello/{name}") {
            val name = pathParameters["name"]
            ok("Hello $name!", contentType = ContentType(PLAIN))
        }
    }
}
// hello_world
```

4. Run the service and view the results at: [http://localhost:2010/hello][Endpoint]

[Gradle Starter]: https://github.com/hexagonkt/gradle_starter
[Maven Starter]: https://github.com/hexagonkt/maven_starter
[Setup Gradle]: https://kotlinlang.org/docs/reference/using-gradle.html
[Setup Maven]: https://kotlinlang.org/docs/reference/using-maven.html
[Endpoint]: http://localhost:2010/hello

## Examples

<details>
<summary>Books Example</summary>

A simple CRUD example showing how to manage book resources. Here you can check the
[full test](http_server/src/test/kotlin/examples/BooksTest.kt).

```kotlin
// books
data class Book(val author: String, val title: String)

private val books: MutableMap<Int, Book> = linkedMapOf(
    100 to Book("Miguel de Cervantes", "Don Quixote"),
    101 to Book("William Shakespeare", "Hamlet"),
    102 to Book("Homer", "The Odyssey")
)

private val path: PathHandler = path {

    post("/books") {
        val queryParameters = request.queryParameters
        val author = queryParameters["author"] ?: return@post badRequest("Missing author")
        val title = queryParameters["title"] ?: return@post badRequest("Missing title")
        val id = (books.keys.maxOrNull() ?: 0) + 1
        books += id to Book(author, title)
        created(id.toString())
    }

    get("/books/{id}") {
        val bookId = pathParameters.require("id").toInt()
        val book = books[bookId]
        if (book != null)
            ok("Title: ${book.title}, Author: ${book.author}")
        else
            notFound("Book not found")
    }

    put("/books/{id}") {
        val bookId = pathParameters.require("id").toInt()
        val book = books[bookId]
        if (book != null) {
            books += bookId to book.copy(
                author = request.queryParameters["author"] ?: book.author,
                title = request.queryParameters["title"] ?: book.title
            )

            ok("Book with id '$bookId' updated")
        }
        else {
            notFound("Book not found")
        }
    }

    delete("/books/{id}") {
        val bookId = pathParameters.require("id").toInt()
        val book = books[bookId]
        books -= bookId
        if (book != null)
            ok("Book with id '$bookId' deleted")
        else
            notFound("Book not found")
    }

    // Matches path's requests with *any* HTTP method as a fallback (return 404 instead 405)
    after(ALL - DELETE - PUT - GET, "/books/{id}", status = NOT_FOUND) {
        send(METHOD_NOT_ALLOWED)
    }

    get("/books") {
        ok(books.keys.joinToString(" ", transform = Int::toString))
    }
}
// books
```
</details>

<details>
<summary>Session Example</summary>

Example showing how to use sessions. Here you can check the
[full test](http_server/src/test/kotlin/examples/SessionTest.kt).

```kotlin
// session
// TODO
// session
```
</details>

<details>
<summary>Error Handling Example</summary>

Code to show how to handle callback exceptions and HTTP error codes. Here you can check the
[full test](http_server/src/test/kotlin/examples/ErrorsTest.kt).

```kotlin
// errors
class CustomException : IllegalArgumentException()

private val path: PathHandler = path {

    // Catching `Exception` handles any unhandled exception before (it has to be the last)
    after(pattern = "*", exception = Exception::class, status = NOT_FOUND) {
        internalServerError("Root handler")
    }

    get("/exception") { throw UnsupportedOperationException("error message") }
    get("/baseException") { throw CustomException() }
    get("/unhandledException") { error("error message") }
    get("/invalidBody") { ok(LocalDateTime.now()) }

    get("/halt") { internalServerError("halted") }
    get("/588") { send(HttpStatus(588)) }

    on(pattern = "*", exception = UnsupportedOperationException::class) {
        val error = context.exception?.message ?: context.exception?.javaClass?.name ?: fail
        val newHeaders = response.headers + ("error" to error)
        send(HttpStatus(599), "Unsupported", headers = newHeaders)
    }

    on(pattern = "*", exception = IllegalArgumentException::class) {
        val error = context.exception?.message ?: context.exception?.javaClass?.name ?: fail
        val newHeaders = response.headers + ("runtime-error" to error)
        send(HttpStatus(598), "Runtime", headers = newHeaders)
    }

    // It is possible to execute a handler upon a given status code before returning
    on(pattern = "*", status = HttpStatus(588)) {
        send(HttpStatus(578), "588 -> 578")
    }
}
// errors
```
</details>

<details>
<summary>Filters Example</summary>

This example shows how to add filters before and after route execution. Here you can check the
[full test](http_server/src/test/kotlin/examples/FiltersTest.kt).

```kotlin
// filters
private val users: Map<String, String> = mapOf(
    "Turing" to "London",
    "Dijkstra" to "Rotterdam"
)

private val path: PathHandler = path {
    filter("*") {
        val start = System.nanoTime()
        // Call next and store result to chain it
        val next = next()
        val time = (System.nanoTime() - start).toString()
        // Copies result from chain with the extra data
        next.send(headers = response.headers + ("time" to time))
    }

    filter("/protected/*") {
        val authorization = request.headers["authorization"]
            ?: return@filter send(UNAUTHORIZED, "Unauthorized")
        val credentials = authorization.removePrefix("Basic ")
        val userPassword = String(credentials.decodeBase64()).split(":")

        // Parameters set in call attributes are accessible in other filters and routes
        send(attributes = attributes
          + ("username" to userPassword[0])
          + ("password" to userPassword[1])
        ).next()
    }

    // All matching filters are run in order unless call is halted
    filter("/protected/*") {
        if(users[attributes["username"]] != attributes["password"])
            send(FORBIDDEN, "Forbidden")
        else
            next()
    }

    get("/protected/hi") {
        ok("Hello ${attributes["username"]}!")
    }

    path("/after") {
        after(PUT) {
            success(ALREADY_REPORTED)
        }

        after(PUT, "/second") {
            success(NO_CONTENT)
        }

        after("/second") {
            success(CREATED)
        }

        after {
            success(ACCEPTED)
        }
    }
}
// filters
```
</details>

<details>
<summary>Files Example</summary>

The following code shows how to serve resources and receive files. Here you can check the
[full test](https://github.com/hexagonkt/hexagon/blob/master/http_server/src/test/kotlin/examples/FilesTest.kt).

```kotlin
// files
private val path: PathHandler = path {

    // Serve `public` resources folder on `/*`
    after(
        methods = setOf(GET),
        pattern = "/*",
        status = NOT_FOUND,
        callback = UrlCallback(URL("classpath:public"))
    )

    path("/static") {
        get("/files/*", UrlCallback(URL("classpath:assets")))
        get("/resources/*", FileCallback(File(directory)))
    }

    get("/html/*", UrlCallback(URL("classpath:assets"))) // Serve `assets` files on `/html/*`
    get("/pub/*", FileCallback(File(directory))) // Serve `test` folder on `/pub/*`

    post("/multipart") {
        val headers: MultiMap<String, String> = request.parts.first().let { p ->
            val name = p.name
            val bodyString = p.bodyString()
            val size = p.size.toString()
            val fullType = p.contentType?.mediaType?.fullType ?: ""
            val contentDisposition = p.headers.require("content-disposition")
            multiMapOf(
                "name" to name,
                "body" to bodyString,
                "size" to size,
                "type" to fullType,
                "content-disposition" to contentDisposition
            )
        }

        ok(headers = headers)
    }

    post("/file") {
        val part = request.parts.first()
        val content = part.bodyString()
        ok(content)
    }

    post("/form") {
      fun serializeMap(map: Map<String, List<String>>): List<String> = listOf(
          map.map { "${it.key}:${it.value.joinToString(",")}}" }.joinToString("\n")
      )

      val queryParams = serializeMap(request.queryParameters.allValues)
      val formParams = serializeMap(request.formParameters.allValues)
      val headers =
          multiMapOfLists("query-params" to queryParams, "form-params" to formParams)

      ok(headers = response.headers + headers)
    }
}
// files
```
</details>

You can check more sample projects and snippets at the [examples page].

[examples page]: https://hexagonkt.com/examples/example_projects

## Thanks

This project is supported by:

<a href="https://www.digitalocean.com/?utm_medium=opensource&utm_source=Hexagon-Toolkit">
  <img
    height="128px"
    src=
      "https://opensource.nyc3.cdn.digitaloceanspaces.com/attribution/assets/SVG/DO_Logo_vertical_blue.svg">
</a>

<a href="https://www.jetbrains.com/?from=Hexagon-Toolkit">
  <img
    height="96px"
    src="https://hexagonkt.com/img/sponsors/jetbrains-variant-4.svg">
</a>

## Status

The toolkit is properly tested. This is the coverage report:

[![Coverage]][CoverageReport]

Performance is not the primary goal, but it is taken seriously. You can check performance numbers
in the [TechEmpower Web Framework Benchmarks][benchmark].

[Coverage]: https://hexagonkt.com/img/coverage.svg
[CoverageReport]: https://hexagonkt.com/jacoco
[benchmark]: https://www.techempower.com/benchmarks

## Contribute

If you like this project and want to support it, the easiest way is to [give it a star] :v:.

If you feel like you can do more. You can contribute to the project in different ways:

* By using it and [spreading the word][@hexagon_kt].
* Giving feedback by [Twitter][@hexagon_kt] or [Slack].
* Requesting [new features or submitting bugs][issues].
* Voting for the features you want in the [issue tracker][issues] (using [reactions]).
* And... Drum roll... Submitting [code or documentation][contributing].

To know what issues are currently open and be aware of the next features you can check the
[Project Board] and the [Organization Board] at GitHub.

You can ask any question, suggestion or complaint at the project's [Slack channel][Slack]. You can
be up to date of project's news following [@hexagon_kt] on Twitter.

Thanks to all project's [contributors]!

[![CodeTriage](https://www.codetriage.com/hexagonkt/hexagon/badges/users.svg)][CodeTriage]

[give it a star]: https://github.com/hexagonkt/hexagon/stargazers
[@hexagon_kt]: https://twitter.com/hexagon_kt
[Slack]: https://kotlinlang.slack.com/messages/hexagon
[issues]: https://github.com/hexagonkt/hexagon/issues
[reactions]: https://github.com/blog/2119-add-reactions-to-pull-requests-issues-and-comments
[contributing]: https://github.com/hexagonkt/hexagon/contribute
[Project Board]: https://github.com/hexagonkt/hexagon/projects/1
[Organization Board]: https://github.com/orgs/hexagonkt/projects/1
[contributors]: https://github.com/hexagonkt/hexagon/graphs/contributors
[CodeTriage]: https://www.codetriage.com/hexagonkt/hexagon

## License

The project is licensed under the [MIT License]. This license lets you use the source for free or
commercial purposes as long as you provide attribution and don’t hold any project member liable.

[MIT License]: license.md
