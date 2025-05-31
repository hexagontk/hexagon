
<h3 align="center">
  <a href="https://hexagontk.com">
    <img alt="Hexagon" src="https://hexagontk.com/stable/icon-small.png" />
  </a>
  <br>
  Hexagon
</h3>

<h4 align="center">The atoms of your platform</h4>

<p align="center">
  <a href="https://github.com/hexagontk/hexagon/actions">
    <img
      alt="GitHub Actions"
      src="https://github.com/hexagontk/hexagon/actions/workflows/release.yml/badge.svg" />
  </a>
  <a href="https://hexagontk.com/stable/jacoco">
    <img src="https://hexagontk.com/stable/img/coverage.svg" alt="Coverage" />
  </a>
  <a href="https://search.maven.org/search?q=g:com.hexagontk">
    <img src="https://hexagontk.com/stable/img/download.svg" alt="Maven Central Repository" />
  </a>
</p>

<p align="center">
  <a href="https://hexagontk.com">Home Site</a> |
  <a href="https://hexagontk.com/stable/quick_start">Quick Start</a>
</p>

---

> [!WARNING]
> Development will not continue to be in the open. Use this toolkit only if you are willing to
> fork it and maintain it by yourself.

## What is Hexagon

Hexagon is a microservices' toolkit (not a [framework]) written in [Kotlin]. Its purpose is to ease
the building of server applications (Web applications, APIs or Serverless handlers) that run inside
a cloud platform.

The Hexagon Toolkit provides several libraries to build server applications. These libraries provide
single standalone features and are referred to as ["Ports"][Ports and Adapters Architecture].

The main ports are:

* [The HTTP server]: supports HTTPS, HTTP/2, WebSockets, mutual TLS, static files (serve and
  upload), forms processing, cookies, CORS and more.
* [The HTTP client]: which supports mutual TLS, HTTP/2, WebSockets, cookies, form fields and files
  among other features.
* [Serialization]: provides a common way of using different data formats. Data formats are pluggable
  and are handled in the same way regardless of their library.
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

* **Native Image**: most of the toolkit libraries include GraalVM metadata (check the [libraries
  catalog]), native tests are run on CI to ensure native images can be built out of the box.

* **Properly Tested**: The project's coverage is checked in every Pull Request. It is also
  stress-tested at [TechEmpower Frameworks Benchmark][benchmark].

For more information check the [Quick Start Guide].

[framework]: https://stackoverflow.com/a/3057818/973418
[Kotlin]: http://kotlinlang.org
[The HTTP server]: http://hexagontk.com/http_server
[The HTTP client]: http://hexagontk.com/http_client
[Serialization]: http://hexagontk.com/serialization
[Template Processing]: http://hexagontk.com/templates
[Hexagonal Architecture]: http://fideloper.com/hexagonal-architecture
[Clean Architecture]: https://8thlight.com/blog/uncle-bob/2012/08/13/the-clean-architecture.html
[Ports and Adapters Architecture]: https://herbertograca.com/2017/09/14/ports-adapters-architecture
[Quick Start Guide]: http://hexagontk.com/quick_start
[libraries catalog]: https://www.graalvm.org/native-image/libraries-and-frameworks

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

    implementation("com.hexagontk.http:http_server_jetty:$hexagonVersion")
    ```

  * In Maven. Declare the dependency in `pom.xml`:

    ```xml
    <dependency>
      <groupId>com.hexagontk.http</groupId>
      <artifactId>http_server_jetty</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

3. Write the code in the `src/main/kotlin/Hello.kt` file:

```kotlin
// hello_world
import com.hexagontk.core.media.TEXT_PLAIN
import com.hexagontk.http.model.ContentType
import com.hexagontk.http.server.HttpServer
import com.hexagontk.http.server.HttpServerSettings
import com.hexagontk.http.server.serve

lateinit var server: HttpServer

/**
 * Start a Hello World server, serving at path "/hello".
 */
fun main() {
    server = serve(JettyServletHttpServer(), HttpServerSettings(bindPort = 0)) {
        get("/hello/{name}") {
            val name = pathParameters["name"]
            ok("Hello $name!", contentType = ContentType(TEXT_PLAIN))
        }
    }
}
// hello_world
```

4. Run the service and view the results at: [http://localhost:2010/hello][Endpoint]

[Gradle Starter]: https://github.com/hexagontk/gradle_starter
[Maven Starter]: https://github.com/hexagontk/maven_starter
[Setup Gradle]: https://kotlinlang.org/docs/reference/using-gradle.html
[Setup Maven]: https://kotlinlang.org/docs/reference/using-maven.html
[Endpoint]: http://localhost:2010/hello

## Examples

<details>
<summary>Books Example</summary>

A simple CRUD example showing how to manage book resources. Here you can check the
[full test](http_test/main/examples/BooksTest.kt).

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
        val author = queryParameters["author"]?.text ?: return@post badRequest("Missing author")
        val title = queryParameters["title"]?.text ?: return@post badRequest("Missing title")
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
                author = queryParameters["author"]?.text ?: book.author,
                title = queryParameters["title"]?.text ?: book.title
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

    // Matches path's requests with *any* HTTP method as a fallback (return 405 instead 404)
    after(ALL - DELETE - PUT - GET, "/books/{id}") {
        send(METHOD_NOT_ALLOWED_405)
    }

    get("/books") {
        ok(books.keys.joinToString(" ", transform = Int::toString))
    }
}
// books
```
</details>

<details>
<summary>Error Handling Example</summary>

Code to show how to handle callback exceptions and HTTP error codes. Here you can check the
[full test](http_test/main/examples/ErrorsTest.kt).

```kotlin
// errors
class CustomException : IllegalArgumentException()

private val path: PathHandler = path {

    /*
     * Catching `Exception` handles any unhandled exception, has to be the last executed (first
     * declared)
     */
    exception<Exception> {
        internalServerError("Root handler")
    }

    exception<IllegalArgumentException> {
        val error = exception?.message ?: exception?.javaClass?.name ?: fail
        val newHeaders = response.headers + Header("runtime-error", error)
        send(598, "Runtime", headers = newHeaders)
    }

    exception<UnsupportedOperationException> {
        val error = exception?.message ?: exception?.javaClass?.name ?: fail
        val newHeaders = response.headers + Header("error", error)
        send(599, "Unsupported", headers = newHeaders)
    }

    get("/exception") { throw UnsupportedOperationException("error message") }
    get("/baseException") { throw CustomException() }
    get("/unhandledException") { error("error message") }
    get("/invalidBody") { ok(LocalDateTime.now()) }

    get("/halt") { internalServerError("halted") }
    get("/588") { send(588) }

    // It is possible to execute a handler upon a given status code before returning
    before(pattern = "*", status = 588) {
        send(578, "588 -> 578")
    }
}
// errors
```
</details>

<details>
<summary>Filters Example</summary>

This example shows how to add filters before and after route execution. Here you can check the
[full test](http_test/main/examples/FiltersTest.kt).

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
        next.send(headers = response.headers + Header("time", time))
    }

    filter("/protected/*") {
        val authorization = request.authorization ?: return@filter unauthorized("Unauthorized")
        val credentials = authorization.body
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
            send(FORBIDDEN_403, "Forbidden")
        else
            next()
    }

    get("/protected/hi") {
        ok("Hello ${attributes["username"]}!")
    }

    path("/after") {
        after(PUT) {
            send(ALREADY_REPORTED_208)
        }

        after(PUT, "/second") {
            send(NO_CONTENT_204)
        }

        after("/second") {
            send(CREATED_201)
        }

        after {
            send(ACCEPTED_202)
        }
    }
}
// filters
```
</details>

<details>
<summary>Files Example</summary>

The following code shows how to serve resources. Here you can check the
[full test](http_test/main/examples/FilesTest.kt).

```kotlin
// files
private val path: PathHandler = path {

    // Serve `public` resources folder on `/*`
    after(
        methods = setOf(GET),
        pattern = "/*",
        status = NOT_FOUND_404,
        callback = UrlCallback(urlOf("classpath:public"))
    )

    path("/static") {
        get("/files/*", UrlCallback(urlOf("classpath:assets")))
        get("/resources/*", FileCallback(File(directory)))
    }

    get("/html/*", UrlCallback(urlOf("classpath:assets"))) // Serve `assets` files on `/html/*`
    get("/pub/*", FileCallback(File(directory))) // Serve `test` folder on `/pub/*`
}
// files
```
</details>
<details>
<summary>Multipart Example</summary>

The following code shows how to receive files. Here you can check the
[full test](http_test/main/examples/FilesTest.kt).

```kotlin
// multipart
private val path: PathHandler = path {

    // Serve `public` resources folder on `/*`
    post("/multipart") {
        val headers = parts.first().let { p ->
            val name = p.name
            val bodyString = p.bodyString()
            val size = p.size.toString()
            Headers(
                Header("name", name),
                Header("body", bodyString),
                Header("size", size),
            )
        }

        ok(headers = headers)
    }

    post("/file") {
        val part = parts.first()
        val content = part.bodyString()
        val submittedFile = part.submittedFileName ?: ""
        ok(content, headers = response.headers + Header("submitted-file", submittedFile))
    }

    post("/form") {
        fun serializeMap(map: Parameters): List<String> = listOf(
            map.all.entries.joinToString("\n") { (k, v) ->
                "$k:${v.joinToString(",") { it.text }}"
            }
        )

        val queryParams = serializeMap(queryParameters).map { Parameter("query-params", it) }
        val formParams = serializeMap(formParameters).map { Parameter("form-params", it) }

        ok(headers = response.headers + Headers(queryParams) + Headers(formParams))
    }
}
// multipart
```
</details>

You can check more sample projects and snippets at the [examples page].

[examples page]: https://hexagontk.com/examples/example_projects

## Status

The toolkit is properly tested. This is the coverage report:

[![Coverage]][CoverageReport]

Performance is not the primary goal, but it is taken seriously. You can check performance numbers
in the [TechEmpower Web Framework Benchmarks][benchmark].

[Coverage]: https://hexagontk.com/stable/img/coverage.svg
[CoverageReport]: https://hexagontk.com/stable/jacoco
[benchmark]: https://www.techempower.com/benchmarks

## Contribute

If you like this project and want to support it, the easiest way is to [give it a star] :v:.

If you feel like you can do more. You can contribute to the project in different ways:

* By using it and [spreading the word][@hexagontk].
* Giving feedback by [X (Twitter)][@hexagontk].
* Requesting [new features or submitting bugs][issues].
* Voting for the features you want in the [issue tracker][issues] (using [reactions]).
* And... Drum roll... Submitting [code or documentation][contributing].

To know what issues are currently open and be aware of the next features you can check the
[Organization Board] at GitHub.

You can ask any question, suggestion or complaint at the project's [discussions]. You can
be up-to-date of project's news following [@hexagontk] on X (Twitter).

Thanks to all project's [contributors]!

[give it a star]: https://github.com/hexagontk/hexagon/stargazers
[discussions]: https://github.com/hexagontk/hexagon/discussions/categories/q-a
[@hexagontk]: https://twitter.com/hexagontk
[issues]: https://github.com/hexagontk/hexagon/issues
[reactions]: https://github.com/blog/2119-add-reactions-to-pull-requests-issues-and-comments
[contributing]: https://github.com/hexagontk/hexagon/contribute
[Organization Board]: https://github.com/orgs/hexagontk/projects/2
[contributors]: https://github.com/hexagontk/hexagon/graphs/contributors

## License

The project is licensed under the [MIT License]. This license lets you use the source for free or
commercial purposes as long as you provide attribution and don’t hold any project member liable.

[MIT License]: license.md
