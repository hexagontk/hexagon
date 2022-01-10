package com.hexagonkt.http.test.examples

import com.hexagonkt.core.helpers.multiMapOfLists
import com.hexagonkt.core.logging.Logger
import com.hexagonkt.core.logging.LoggingLevel.DEBUG
import com.hexagonkt.core.logging.LoggingLevel.OFF
import com.hexagonkt.core.logging.LoggingManager
import com.hexagonkt.core.media.ApplicationMedia.JSON
import com.hexagonkt.core.media.ApplicationMedia.XML
import com.hexagonkt.core.media.TextMedia
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.model.HttpClientRequest
import com.hexagonkt.http.client.model.HttpClientResponse
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.ClientErrorStatus.*
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.HttpMethod.Companion.ALL
import com.hexagonkt.http.model.RedirectionStatus.FOUND
import com.hexagonkt.http.model.ServerErrorStatus.HTTP_VERSION_NOT_SUPPORTED
import com.hexagonkt.http.model.ServerErrorStatus.INTERNAL_SERVER_ERROR
import com.hexagonkt.http.model.SuccessStatus.OK
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.http.server.callbacks.UrlCallback
import com.hexagonkt.http.server.handlers.path
import com.hexagonkt.http.server.serve
import com.hexagonkt.logging.slf4j.jul.Slf4jJulLoggingAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.lang.RuntimeException
import java.net.InetAddress
import java.net.URL
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class SamplesTest(
    val clientAdapter: () -> HttpClientPort,
    val serverAdapter: () -> HttpServerPort
) {

    private val logger: Logger = Logger(SamplesTest::class)

    @BeforeAll fun startUp() {
        LoggingManager.adapter = Slf4jJulLoggingAdapter()
        LoggingManager.setLoggerLevel("com.hexagonkt", DEBUG)
    }

    @AfterAll fun shutDown() {
        LoggingManager.setLoggerLevel("com.hexagonkt", OFF)
    }

    @Test fun serverCreation() = runBlocking {
        // serverCreation
        /*
         * All settings are optional, you can supply any combination
         * Parameters not set will fall back to the defaults
         */
        val settings = HttpServerSettings(
            bindAddress = withContext(Dispatchers.IO) { InetAddress.getByName("0.0.0") },
            bindPort = 2020,
            contextPath = "/context",
            banner = "name"
        )

        val path = path {
            get("/hello") { ok("Hello World!") }
        }

        serve(serverAdapter(), listOf(path), settings).use { server ->
            HttpClient(clientAdapter(), URL("http://localhost:${server.runtimePort}")).use {
                it.start()
                assert(server.started())
                assertEquals("Hello World!", it.get("/context/hello").body)
            }
        }

        /*
         * You can skip the adapter is you previously bound one
         * You may also skip the settings and the defaults will be used
         */
        serve(serverAdapter(), listOf(path)).use { server ->
            HttpClient(clientAdapter(), URL("http://localhost:${server.runtimePort}")).use {
                it.start()
                assert(server.started())
                assertEquals("Hello World!", it.get("/hello").body)
            }
        }
        // serverCreation
    }

    @Test fun routesCreation() = runBlocking {
        val server = serve(serverAdapter()) {
            // routesCreation
            get("/hello") { ok("Get greeting") }
            put("/hello") { ok("Put greeting") }
            post("/hello") { ok("Post greeting") }

            on(ALL - GET - PUT - POST, "/hello") { ok("Fallback if HTTP verb was not used before") }

            on(status = NOT_FOUND) { ok("Get at '/' if no route matched before") }
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

    @Test fun routeGroups() = runBlocking {
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

    @Test fun routers() = runBlocking {
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

    @Suppress("UNREACHABLE_CODE")
    @Test fun callbacks() = runBlocking {
        val server = HttpServer(serverAdapter()) {
            // callbackCall
            get("/call") {
                attributes                   // the attributes list
                attributes["foo"]            // value of foo attribute

                ok("Response body")          // returns a 200 status
                // return any status
                send(
                    BAD_REQUEST,
                    "Invalid request",
                    attributes = attributes + ("A" to "V") // sets value of attribute A to V
                )
            }
            // callbackCall

            // callbackRequest
            get("/request") {
                // URL Information
                request.method                   // the HTTP method (GET, ..etc)
                request.protocol                 // http or https TODO
                request.host                     // the host, e.g. "example.com"
                request.port                     // the server port
                request.path                     // the request path, e.g. /result.jsp
                request.body                     // request body sent by the client

                // Headers
                request.headers                  // the HTTP header list with first values only
                request.headers["BAR"]           // first value of BAR header
                request.headers.allValues        // the HTTP header list with their full values list
                request.headers.allValues["BAR"] // list of values of BAR header

                // Common headers shortcuts
                request.contentType              // content type of request.body
                request.accept                   // Client accepted content types
                request.userAgent()              // user agent (browser requests)
                request.origin()                 // origin (browser requests)
                request.referer()                // TODO

                // Parameters
                pathParameters                    // map with all path parameters
                request.formParameters            // map with first values of all form fields
                request.formParameters.allValues  // map with all form fields values
                request.queryParameters           // map with first values of all query parameters
                request.queryParameters.allValues // map with all query parameters values

                // Body processing
                request.contentLength             // length of request body

                ok()
            }
            // callbackRequest

            // callbackResponse
            get("/response") {
                response.body                        // get response content
                response.status                      // get the response status
                response.contentType                 // get the content type
                send(
                    status = UNAUTHORIZED,           // set status code to 401
                    body = "Hello",                  // sets content to Hello
                    contentType = ContentType(XML),  // set content type to application/xml
                    headers = response.headers
                        + ("foo" to "bar")           // sets header FOO with single value bar
                        + multiMapOfLists("baz" to listOf("1", "2")) // sets header FOO values with [ bar ]
                )
            }
            // callbackResponse

            // callbackPathParam
            get("/pathParam/{foo}") {
                pathParameters["foo"] // value of foo path parameter
                pathParameters        // map with all parameters
                ok()
            }
            // callbackPathParam

            // callbackQueryParam
            get("/queryParam") {
                request.queryString
                request.queryParameters                       // the query param list
                request.queryParameters["FOO"]                // value of FOO query param
                request.queryParameters.allValues             // the query param list
                request.queryParameters.allValues["FOO"]      // all values of FOO query param
                ok()
            }
            // callbackQueryParam

            // callbackFormParam
            get("/formParam") {
                request.formParameters                       // the query param list
                request.formParameters["FOO"]                // value of FOO query param
                request.formParameters.allValues             // the query param list
                request.formParameters.allValues["FOO"]      // all values of FOO query param
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
                redirect(FOUND, "/call") // browser redirect to /call
            }
            // callbackRedirect

            // callbackCookie
            get("/cookie") {
                request.cookies                       // get map of all request cookies
                request.cookiesMap()["foo"]           // access request cookie by name

                val cookie = HttpCookie("new_foo", "bar")
                ok(
                    cookies = listOf(
                        cookie,                     // set cookie with a value
                        cookie.copy(maxAge = 3600), // set cookie with a max-age
                        cookie.copy(secure = true), // secure cookie
                        cookie.delete(),            // remove cookie
                    )
                )
            }
            // callbackCookie

            // callbackHalt
            get("/halt") {
                clientError(UNAUTHORIZED)             // halt with status
                clientError(UNAUTHORIZED, "Go away!") // halt with status and message
                internalServerError("Body Message")   // halt with message (status 500)
                internalServerError()                 // halt with status 500
            }
            // callbackHalt

            after(exception = Exception::class) {
                logger.error(this.context.exception ?: RuntimeException()) { "" }
                internalServerError(this.context.exception?.message ?: "Error")
            }
        }

        server.use { s ->
            s.start()
            HttpClient(clientAdapter(), URL("http://localhost:${s.runtimePort}")).use {
                it.cookies += HttpCookie("foo", "bar")
                it.start()

                val callResponse = it.get("/call")
                assertEquals(BAD_REQUEST, callResponse.status)
                assertEquals("Invalid request", callResponse.body)
                assertEquals(
                    OK,
                    it.get("/request", body = "body", contentType = ContentType(JSON)).status
                )

                assertEquals(UNAUTHORIZED, it.get("/response").status)
                assertEquals(OK, it.get("/pathParam/param").status)
                assertEquals(OK, it.get("/queryParam").status)
                assertEquals(OK, it.get("/formParam").status)
                assertEquals(FOUND, it.get("/redirect").status)
                assertEquals(OK, it.get("/cookie").status)
                assertEquals(INTERNAL_SERVER_ERROR, it.get("/halt").status)

                val stream = URL("classpath:assets/index.html").readBytes()
                val parts = listOf(HttpPart("file", stream, "index.html"))
                val response = it.send(HttpClientRequest(POST, path = "/file", parts = parts))
                assert(response.bodyString().contains("<title>Hexagon</title>"))

                it.stop()
            }
        }
    }

    @Test fun filters() = runBlocking {
        fun assertResponse(response: HttpClientResponse, body: String, vararg headers: String) {
            assertEquals(OK, response.status)
            assertEquals(body, response.body)
            (headers.toList() + "b-all" + "a-all").forEach {
                assert(response.headers.containsKey(it))
            }
        }

        fun assertFail(
            code: HttpStatus, response: HttpClientResponse, body: String, vararg headers: String) {

            assertEquals(code, response.status)
            (headers.toList() + "b-all" + "a-all").forEach { assert(response.headers.contains(it)) }
            assertEquals(body, response.body)
        }

        val server = HttpServer(serverAdapter()) {
            // filters
            on("/*") { send(headers = response.headers + ("b-all" to "true")) }

            on("/filters/*") { send(headers = response.headers + ("b-filters" to "true")) }
            get("/filters/route") { ok("filters route") }
            after("/filters/*") { send(headers = response.headers + ("a-filters" to "true")) }

            get("/filters") { ok("filters") }

            path("/nested") {
                on("*") { send(headers = response.headers + ("b-nested" to "true")) }
                on { send(headers = response.headers + ("b-nested-2" to "true")) }
                get("/filters") { ok("nested filters") }
                get("/halted") { send(HttpStatus(499), "halted") }
                get { ok("nested also") }
                after("*") { send(headers = response.headers + ("a-nested" to "true")) }
            }

            after("/*") { send(headers = response.headers + ("a-all" to "true")) }
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
                assert(!it.get("/filters/route").headers.contains("b-nested"))
                assert(!it.get("/filters/route").headers.contains("a-nested"))
            }
        }
    }

    @Test fun errors() = runBlocking {
        val server = serve(serverAdapter()) {
            // errors
            // Register handler for routes halted with 512 code
            get("/errors") { send(HttpStatus(512)) }

            on(pattern = "*", status = HttpStatus(512)) { send(INTERNAL_SERVER_ERROR, "Ouch") }
            // errors

            // exceptions
            // Register handler for routes which callbacks throw exceptions
            get("/exceptions") { error("Message") }
            get("/codedExceptions") { send(HttpStatus(509), "code") }

            on(pattern = "*", status = HttpStatus(509)) {
                send(HttpStatus(599))
            }
            on(pattern = "*", exception = IllegalStateException::class) {
                send(HTTP_VERSION_NOT_SUPPORTED, context.exception?.message ?: "empty")
            }
            // exceptions
        }

        server.use { s ->
            HttpClient(clientAdapter(), URL("http://localhost:${s.runtimePort}")).use {
                it.start()

                val errors = it.get("/errors")
                assertEquals(INTERNAL_SERVER_ERROR, errors.status)
                assertEquals("Ouch", errors.body)

                val exceptions = it.get("/exceptions")
                assertEquals(HTTP_VERSION_NOT_SUPPORTED, exceptions.status)
                assertEquals("Message", exceptions.body)

                val codedExceptions = it.get("/codedExceptions")
                assertEquals(HttpStatus(599), codedExceptions.status)
                assertEquals("code", codedExceptions.body)
            }
        }
    }

    @Test fun files() = runBlocking {
        val server = serve(serverAdapter()) {
            // files
            get("/web/file.txt") { ok("It matches this route and won't search for the file") }

            // Expose resources on the '/public' resource folder over the '/web' HTTP path
            on(
                status = NOT_FOUND,
                pattern = "/web/*",
                callback = UrlCallback(URL("classpath:public"))
            )

            // Maps resources on 'assets' on the server root (assets/f.css -> /f.css)
            // '/public/css/style.css' resource would be: 'http://{host}:{port}/css/style.css'
            on(status = NOT_FOUND, pattern = "/*", callback = UrlCallback(URL("classpath:assets")))
            // files
        }

        server.use { s ->
            HttpClient(clientAdapter(), URL("http://localhost:${s.runtimePort}")).use {
                it.start()

                assert(it.get("/web/file.txt").bodyString().startsWith("It matches this route"))

                val index = it.get("/index.html")
                assertEquals(OK, index.status)
                assertEquals(ContentType(TextMedia.HTML), index.contentType)
                val file = it.get("/web/file.css")
                assertEquals(OK, file.status)
                assertEquals(ContentType(TextMedia.CSS), file.contentType)

                val unavailable = it.get("/web/unavailable.css")
                assertEquals(NOT_FOUND, unavailable.status)
            }
        }
    }

    @Test fun test() = runBlocking {
        // test
        val router = path {
            get("/hello") { ok("Hi!") }
        }

        val bindAddress = InetAddress.getLoopbackAddress()
        val serverSettings = HttpServerSettings(bindAddress, 0, banner = "name")
        val server = serve(serverAdapter(), listOf(router), serverSettings)

        server.use { s ->
            HttpClient(clientAdapter(), URL("http://localhost:${s.runtimePort}")).use {
                it.start()
                assertEquals("Hi!", it.get("/hello").body)
            }
        }
        // test
    }
}
