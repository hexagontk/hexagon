package com.hexagonkt.http.test.examples

import com.hexagonkt.core.logging.LoggingLevel.DEBUG
import com.hexagonkt.core.logging.LoggingLevel.OFF
import com.hexagonkt.core.logging.LoggingManager
import com.hexagonkt.core.media.APPLICATION_JSON
import com.hexagonkt.core.media.APPLICATION_XML
import com.hexagonkt.core.media.TEXT_CSS
import com.hexagonkt.core.media.TEXT_HTML
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.model.HttpClientRequest
import com.hexagonkt.http.client.model.HttpClientResponse
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.HttpMethod.Companion.ALL
import com.hexagonkt.http.model.FOUND_302
import com.hexagonkt.http.model.HTTP_VERSION_NOT_SUPPORTED_505
import com.hexagonkt.http.model.INTERNAL_SERVER_ERROR_500
import com.hexagonkt.http.model.OK_200
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.http.server.callbacks.UrlCallback
import com.hexagonkt.http.server.handlers.*
import com.hexagonkt.http.server.serve
import com.hexagonkt.logging.slf4j.jul.Slf4jJulLoggingAdapter
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.net.InetAddress
import java.net.URL
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
abstract class SamplesTest(
    val clientAdapter: () -> HttpClientPort,
    val serverAdapter: () -> HttpServerPort,
    val serverSettings: HttpServerSettings = HttpServerSettings(),
) {

    @BeforeAll fun startUp() {
        LoggingManager.adapter = Slf4jJulLoggingAdapter()
        LoggingManager.setLoggerLevel("com.hexagonkt", DEBUG)
    }

    @AfterAll fun shutDown() {
        LoggingManager.setLoggerLevel("com.hexagonkt", OFF)
    }

    @Test fun serverCreation() {
        // serverCreation
        /*
         * All settings are optional, you can supply any combination
         * Parameters not set will fall back to the defaults
         */
        val settings = HttpServerSettings(
            bindAddress = InetAddress.getByName("0.0.0"),
            bindPort = 2020,
            contextPath = "/context",
            banner = "name"
        )

        val path = path {
            get("/hello") { ok("Hello World!") }
        }

        val runningServer = serve(serverAdapter(), path, settings)

        // Servers implement closeable, you can use them inside a block assuring they will be closed
        runningServer.use { s ->
            HttpClient(clientAdapter(), URL("http://localhost:${s.runtimePort}")).use {
                it.start()
                assert(s.started())
                assertEquals("Hello World!", it.get("/context/hello").body)
            }
        }

        /*
         * You may skip the settings and the defaults will be used
         */
        val defaultSettingsServer = serve(serverAdapter(), path)
        // serverCreation

        defaultSettingsServer.use { s ->
            HttpClient(clientAdapter(), URL("http://localhost:${s.runtimePort}")).use {
                it.start()
                assert(s.started())
                assertEquals("Hello World!", it.get("/hello").body)
            }
        }
    }

    @Test fun routesCreation() {
        val server = serve(serverAdapter()) {
            // routesCreation
            get("/hello") { ok("Get greeting") }
            put("/hello") { ok("Put greeting") }
            post("/hello") { ok("Post greeting") }

            on(ALL - GET - PUT - POST, "/hello") { ok("Fallback if HTTP verb was not used before") }

            on(status = NOT_FOUND_404) { ok("Get at '/' if no route matched before") }
            // routesCreation
        }

        server.use { s ->
            HttpClient(clientAdapter(), URL("http://localhost:${s.runtimePort}")).use {
                it.start()
                assertEquals("Get greeting", it.get("/hello").body)
                assertEquals("Put greeting", it.put("/hello").body)
                assertEquals("Post greeting", it.post("/hello").body)
                assertEquals("Fallback if HTTP verb was not used before", it.options("/hello").body)
                assertEquals("Get at '/' if no route matched before", it.get("/").body)
            }
        }
    }

    @Test fun routeGroups() {
        val server = serve(serverAdapter()) {
            // routeGroups
            path("/nested") {
                get("/hello") { ok("Greeting") }

                path("/secondLevel") {
                    get("/hello") { ok("Second level greeting") }
                }

                get { ok("Get at '/nested'") }
            }
            // routeGroups
        }

        server.use { s ->
            HttpClient(clientAdapter(), URL("http://localhost:${s.runtimePort}")).use {
                it.start()
                assertEquals("Greeting", it.get("/nested/hello").body)
                assertEquals("Second level greeting", it.get("/nested/secondLevel/hello").body)
                assertEquals("Get at '/nested'", it.get("/nested").body)
            }
        }
    }

    @Test fun routers() {
        // routers
        fun personRouter(kind: String) = path {
            get { ok("Get $kind") }
            put { ok("Put $kind") }
            post { ok("Post $kind") }
        }

        val server = HttpServer(serverAdapter()) {
            path("/clients", personRouter("client"))
            path("/customers", personRouter("customer"))
        }
        // routers

        server.use { s ->
            s.start()
            HttpClient(clientAdapter(), URL("http://localhost:${server.runtimePort}")).use {
                it.start()

                assertEquals("Get client", it.get("/clients").body)
                assertEquals("Put client", it.put("/clients").body)
                assertEquals("Post client", it.post("/clients").body)

                assertEquals("Get customer", it.get("/customers").body)
                assertEquals("Put customer", it.put("/customers").body)
                assertEquals("Post customer", it.post("/customers").body)
            }
        }
    }

    @Test fun callbacks() {
        val server = HttpServer(serverAdapter()) {
            // callbackCall
            get("/call") {
                attributes          // The attributes list
                attributes["foo"]   // Value of foo attribute

                ok("Response body") // Returns a 200 status
                // return any status (previous return value is ignored here)
                send(
                    BAD_REQUEST_400,
                    "Invalid request",
                    attributes = attributes + ("A" to "V") // Sets value of attribute A to V
                )
            }
            // callbackCall

            // callbackRequest
            get("/request") {
                // URL Information
                request.method                   // The HTTP method (GET, ..etc)
                request.protocol                 // HTTP or HTTPS
                request.host                     // The host, e.g. "example.com"
                request.port                     // The server port
                request.path                     // The request path, e.g. /result.jsp
                request.body                     // Request body sent by the client

                method                           // Shortcut of `request.method`
                protocol                         // Shortcut of `request.protocol`
                host                             // Shortcut of `request.host`
                port                             // Shortcut of `request.port`
                path                             // Shortcut of `request.path`

                // Headers
                request.headers                  // The HTTP headers map
                request.headers["BAR"]?.value    // First value of BAR header
                request.headers["BAR"]?.values   // List of values of BAR header

                // Common headers shortcuts
                request.contentType              // Content type of request.body
                request.accept                   // Client accepted content types
                request.authorization            // Client authorization
                request.userAgent()              // User agent (browser requests)
                request.origin()                 // Origin (browser requests)
                request.referer()                // Referer header (page that makes the request)

                accept                           // Shortcut of `request.accept`
                authorization                    // Shortcut of `request.authorization`

                // Parameters
                pathParameters                    // Map with all path parameters
                request.formParameters            // Map with all form fields
                request.queryParameters           // Map with all query parameters

                queryParameters                   // Shortcut of `request.queryParameters`
                formParameters                    // Shortcut of `request.formParameters`

                // Body processing
                request.contentLength             // Length of request body

                ok()
            }
            // callbackRequest

            // callbackResponse
            get("/response") {
                response.body                       // Get response content
                response.status                     // Get the response status
                response.contentType                // Get the content type

                status                              // Shortcut of `response.status`

                send(
                    status = UNAUTHORIZED_401,                  // Set status code to 401
                    body = "Hello",                             // Sets content to Hello
                    contentType = ContentType(APPLICATION_XML), // Set application/xml content type
                    headers = response.headers
                        + Header("foo", "bar")      // Sets header FOO with single value bar
                        + Header("baz", "1", "2")   // Sets header FOO values with [ bar ]
                )
            }
            // callbackResponse

            // callbackPathParam
            get("/pathParam/{foo}") {
                pathParameters["foo"] // Value of foo path parameter
                pathParameters        // Map with all parameters
                ok()
            }
            // callbackPathParam

            // callbackQueryParam
            get("/queryParam") {
                request.queryParameters                      // The query params map
                request.queryParameters["FOO"]?.value        // Value of FOO query param
                request.queryParameters["FOO"]?.values       // All values of FOO query param
                request.queryParameters.values               // The query params list

                ok()
            }
            // callbackQueryParam

            // callbackFormParam
            get("/formParam") {
                request.formParameters                       // The form params map
                request.formParameters["FOO"]?.value         // Value of FOO form param
                request.formParameters["FOO"]?.values        // All values of FOO form param
                request.formParameters.values                // The form params list
                ok()
            }
            // callbackFormParam

            // callbackFile
            post("/file") {
                val filePart = request.partsMap()["file"] ?: error("File not available")
                ok(filePart.body)
            }
            // callbackFile

            // callbackRedirect
            get("/redirect") {
                send(FOUND_302, "/call") // browser redirect to /call
            }
            // callbackRedirect

            // callbackCookie
            get("/cookie") {
                request.cookies                     // Get map of all request cookies
                request.cookiesMap()["foo"]         // Access request cookie by name

                val cookie = Cookie("new_foo", "bar")
                ok(
                    cookies = listOf(
                        cookie,                     // Set cookie with a value
                        cookie.copy(maxAge = 3600), // Set cookie with a max-age
                        cookie.copy(secure = true), // Secure cookie
                        cookie.delete(),            // Remove cookie
                    )
                )
            }
            // callbackCookie

            // callbackHalt
            get("/halt") {
                send(UNAUTHORIZED_401)              // Halt with status
                send(UNAUTHORIZED_401, "Go away!")  // Halt with status and message
                internalServerError("Body Message") // Halt with message (status 500)
                internalServerError()               // Halt with status 500
            }
            // callbackHalt
        }

        server.use { s ->
            s.start()
            HttpClient(clientAdapter(), URL("http://localhost:${s.runtimePort}")).use {
                it.cookies += Cookie("foo", "bar")
                it.start()

                val json = ContentType(APPLICATION_JSON)
                val callResponse = it.get("/call")
                assertEquals(BAD_REQUEST_400, callResponse.status)
                assertEquals("Invalid request", callResponse.body)
                assertEquals(OK_200, it.get("/request", body = "body", contentType = json).status)

                assertEquals(UNAUTHORIZED_401, it.get("/response").status)
                assertEquals(OK_200, it.get("/pathParam/param").status)
                assertEquals(OK_200, it.get("/queryParam").status)
                assertEquals(OK_200, it.get("/formParam").status)
                assertEquals(FOUND_302, it.get("/redirect").status)
                assertEquals(OK_200, it.get("/cookie").status)
                assertEquals(INTERNAL_SERVER_ERROR_500, it.get("/halt").status)

                val stream = URL("classpath:assets/index.html").readBytes()
                val parts = listOf(HttpPart("file", stream, "index.html"))
                val response = it.send(HttpClientRequest(POST, path = "/file", parts = parts))
                assert(response.bodyString().contains("<title>Hexagon</title>"))
            }
        }
    }

    @Test fun filters() {
        fun assertResponse(response: HttpClientResponse, body: String, vararg headers: String) {
            assertEquals(OK_200, response.status)
            assertEquals(body, response.body)
            (headers.toList() + "b-all" + "a-all").forEach {
                assert(response.headers.httpFields.containsKey(it))
            }
        }

        fun assertFail(
            code: HttpStatus, response: HttpClientResponse, body: String, vararg headers: String) {

            assertEquals(code, response.status)
            (headers.toList() + "b-all" + "a-all")
                .forEach { assert(response.headers.httpFields.contains(it)) }
            assertEquals(body, response.body)
        }

        val server = HttpServer(serverAdapter()) {
            // filters
            on("/*") { send(headers = response.headers + Header("b-all", "true")) }

            on("/filters/*") { send(headers = response.headers + Header("b-filters", "true")) }
            get("/filters/route") { ok("filters route") }
            after("/filters/*") { send(headers = response.headers + Header("a-filters", "true")) }

            get("/filters") { ok("filters") }

            path("/nested") {
                on("*") { send(headers = response.headers + Header("b-nested", "true")) }
                on { send(headers = response.headers + Header("b-nested-2", "true")) }
                get("/filters") { ok("nested filters") }
                get("/halted") { send(HttpStatus(499), "halted") }
                get { ok("nested also") }
                after("*") { send(headers = response.headers + Header("a-nested", "true")) }
            }

            after("/*") { send(headers = response.headers + Header("a-all", "true")) }
            // filters
        }

        server.use { s ->
            s.start()

            HttpClient(clientAdapter(), URL("http://localhost:${server.runtimePort}")).use {
                it.start()
                assertResponse(it.get("/filters/route"), "filters route", "b-filters", "a-filters")
                assertResponse(it.get("/filters"), "filters")
                assertResponse(it.get("/nested/filters"), "nested filters", "b-nested", "a-nested")
                val responseNested = it.get("/nested")
                assertResponse(responseNested, "nested also", "b-nested", "b-nested-2", "a-nested")
                val responseHalted = it.get("/nested/halted")
                assertFail(HttpStatus(499), responseHalted, "halted", "b-nested", "a-nested")
                assert(!it.get("/filters/route").headers.httpFields.contains("b-nested"))
                assert(!it.get("/filters/route").headers.httpFields.contains("a-nested"))
            }
        }
    }

    @Test fun errors() {
        class NumberException (val number: Int) : RuntimeException()

        val server = serve(serverAdapter()) {
            // errors
            exception<Exception>(NOT_FOUND_404) {
                internalServerError("Root handler")
            }

            // Register handler for routes halted with 512 code
            get("/errors") { send(HttpStatus(512)) }

            on(pattern = "*", status = HttpStatus(512)) { send(INTERNAL_SERVER_ERROR_500, "Ouch") }
            // errors

            exception<NumberException> { e ->
                internalServerError(e.number.toString())
            }

            get("/codeException") { throw NumberException(9) }

            // exceptions
            // Register handler for routes which callbacks throw exceptions
            get("/exceptions") { error("Message") }
            get("/codedExceptions") { send(HttpStatus(509), "code") }

            on(pattern = "*", status = HttpStatus(509)) {
                send(HttpStatus(599))
            }
            on(pattern = "*", exception = IllegalStateException::class) {
                send(HTTP_VERSION_NOT_SUPPORTED_505, exception?.message ?: "empty")
            }
            // exceptions
        }

        server.use { s ->
            HttpClient(clientAdapter(), URL("http://localhost:${s.runtimePort}")).use {
                it.start()

                val errors = it.get("/errors")
                assertEquals(INTERNAL_SERVER_ERROR_500, errors.status)
                assertEquals("Ouch", errors.body)

                val exceptions = it.get("/exceptions")
                assertEquals(HTTP_VERSION_NOT_SUPPORTED_505, exceptions.status)
                assertEquals("Message", exceptions.body)

                val codedExceptions = it.get("/codedExceptions")
                assertEquals(HttpStatus(599), codedExceptions.status)
                assertEquals("code", codedExceptions.body)

                it.get("/codeException").apply {
                    assertEquals(INTERNAL_SERVER_ERROR_500, status)
                    assertEquals("9", bodyString())
                }
            }
        }
    }

    @Test fun files() {
        val server = serve(serverAdapter()) {
            // files
            get("/web/file.txt") { ok("It matches this route and won't search for the file") }

            // Expose resources on the '/public' resource folder over the '/web' HTTP path
            on(
                status = NOT_FOUND_404,
                pattern = "/web/*",
                callback = UrlCallback(URL("classpath:public"))
            )

            // Maps resources on 'assets' on the server root (assets/f.css -> /f.css)
            // '/public/css/style.css' resource would be: 'http://{host}:{port}/css/style.css'
            on(status = NOT_FOUND_404, pattern = "/*", callback = UrlCallback(URL("classpath:assets")))
            // files
        }

        server.use { s ->
            HttpClient(clientAdapter(), URL("http://localhost:${s.runtimePort}")).use {
                it.start()

                assert(it.get("/web/file.txt").bodyString().startsWith("It matches this route"))

                val index = it.get("/index.html")
                assertEquals(OK_200, index.status)
                assertEquals(ContentType(TEXT_HTML), index.contentType)
                val file = it.get("/web/file.css")
                assertEquals(OK_200, file.status)
                assertEquals(ContentType(TEXT_CSS), file.contentType)

                val unavailable = it.get("/web/unavailable.css")
                assertEquals(NOT_FOUND_404, unavailable.status)
            }
        }
    }

    @Test fun test() {
        // test
        val router = path {
            get("/hello") { ok("Hi!") }
        }

        val bindAddress = InetAddress.getLoopbackAddress()
        val serverSettings = HttpServerSettings(bindAddress, 0, banner = "name")
        val server = serve(serverAdapter(), router, serverSettings)

        server.use { s ->
            HttpClient(clientAdapter(), URL("http://localhost:${s.runtimePort}")).use {
                it.start()
                assertEquals("Hi!", it.get("/hello").body)
            }
        }
        // test
    }

    @Test fun mockRequest() {
        // mockRequest
        // Test callback (basically, a handler without a predicate)
        val callback: HttpCallback = {
            val fakeAttribute = attributes["fake"]
            val fakeHeader = request.headers["fake"]?.value
            ok("Callback result $fakeAttribute $fakeHeader")
        }

        // You can test callbacks with fake data
        val resultContext = callback.process(
            attributes = mapOf("fake" to "attribute"),
            headers = Headers(Header("fake", "header"))
        )

        assertEquals("Callback result attribute header", resultContext.response.bodyString())

        // Handlers can also be tested to check predicates along the callbacks
        val handler = get("/path", callback)

        val notFound = handler.process()
        val ok = handler.process(method = GET, path = "/path")

        assertEquals(NOT_FOUND_404, notFound.status)
        assertEquals(OK_200, ok.status)
        assertEquals("Callback result null null", ok.bodyString())
        // mockRequest
    }
}
