package com.hexagonkt.http.client

import com.hexagonkt.helpers.logger
import com.hexagonkt.helpers.require
import com.hexagonkt.http.Protocol.HTTPS
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.server.Call
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerSettings
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import com.hexagonkt.http.server.serve
import com.hexagonkt.injection.InjectionManager
import com.hexagonkt.serialization.Json
import com.hexagonkt.serialization.SerializationFormat
import com.hexagonkt.serialization.Yaml
import com.hexagonkt.serialization.serialize

import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.net.URI

@Test abstract class ClientTest(private val adapter: ClientPort) {

    private var handler: Call.() -> Unit = {}

    private val server: Server by lazy {
        Server(JettyServletAdapter()) {
            post("/*") { handler() }
            get("/*") { handler() }
            head("/*") { handler() }
            put("/*") { handler() }
            delete("/*") { handler() }
            trace("/*") { handler() }
            options("/*") { handler() }
            patch("/*") { handler() }
        }
    }

    init {
        InjectionManager.bindObject(ClientPort::class, adapter)
    }

    private val client by lazy {
        Client("http://localhost:${server.runtimePort}", ClientSettings(Json))
    }

    @BeforeClass fun startup() {
        server.start()
    }

    @AfterClass fun shutdown() {
        server.stop()
    }

    @BeforeMethod fun resetHandler() {
        handler = {
            response.headers["content-type"] = "application/json;charset=utf-8"
            response.headers["body"] = request.body
            ok(request.body)
        }
    }

    @Test fun `JSON requests works as expected`() {
        val expectedBody = "{\n  \"foo\" : \"fighters\",\n  \"es\" : \"áéíóúÁÉÍÓÚñÑ\"\n}"
        val requestBody = mapOf("foo" to "fighters", "es" to "áéíóúÁÉÍÓÚñÑ")

        val body = client.post("/", requestBody, Json.contentType).body
        assert(body.toString().trim() == expectedBody)

        val body2 = client.post("/", body = requestBody).body
        assert(body2.toString().trim() == expectedBody)

        client.get("/")
        client.get("/")
    }

    @Test fun `HTTP methods with objects work ok`() {
        val parameter = mapOf("key" to "value")
        checkResponse(client.get("/"), null)
        checkResponse(client.head("/"), null)
        checkResponse(client.post("/"), null)
        checkResponse(client.put("/"), null)
        checkResponse(client.delete("/"), null)
        checkResponse(client.trace("/"), null)
        checkResponse(client.options("/"), null)
        checkResponse(client.patch("/"), null)
        checkResponse(client.get("/", body = parameter), parameter)
        checkResponse(client.post("/", parameter), parameter)
        checkResponse(client.put("/", parameter), parameter)
        checkResponse(client.delete("/", parameter), parameter)
        checkResponse(client.trace("/", parameter), parameter)
        checkResponse(client.options("/", parameter), parameter)
        checkResponse(client.patch("/", parameter), parameter)
        checkResponse(client.get("/", body = parameter, format = Yaml), parameter, Yaml)
        checkResponse(client.post("/", parameter, Yaml), parameter, Yaml)
        checkResponse(client.put("/", parameter, Yaml), parameter, Yaml)
        checkResponse(client.delete("/", parameter, Yaml), parameter, Yaml)
        checkResponse(client.trace("/", parameter, Yaml), parameter, Yaml)
        checkResponse(client.options("/", parameter, Yaml), parameter, Yaml)
        checkResponse(client.patch("/", parameter, Yaml), parameter, Yaml)
    }

    @Test fun `Parameters are set properly` () {
        val endpoint = "http://localhost:${server.runtimePort}"
        val h = mapOf("header1" to listOf("val1", "val2"))
        val settings = ClientSettings(Json.contentType, false, h, "user", "password", true)
        val c = Client(adapter, endpoint, settings)

        assert(c.settings.contentType == Json.contentType)
        assert(!c.settings.useCookies)
        assert(c.settings.headers == h)

        handler = {
            response.headersValues["auth"] = listOf(request.headers.require("Authorization"))
            response.headersValues["head1"] = request.headersValues.require("header1")
        }

        c.get("/auth").apply {
            assert(headers["auth"]?.firstOrNull()?.startsWith("Basic") ?: false)
            assert(headers["head1"]?.contains("val1") ?: false)
            assert(headers["head1"]?.contains("val2") ?: false)
            assert(status == 200)
        }
    }

    @Test fun `Files are sent in base64` () {
        handler = { response.headersValues["file64"] = listOf(request.body) }

        val file = File("src/test/resources/logback-test.xml").let {
            if (it.exists()) it
            else File("port_http_client/src/test/resources/logback-test.xml")
        }

        val r = client.post("/file", file)
        assert(r.headers.require("file64").isNotEmpty())
        assert(r.status == 200)
    }

    @Test fun `Integers are sent properly` () {
        var run: Boolean

        client.post("/string", 42).apply {
            assert(headers.require("body").isNotEmpty())
            assert(status == 200)
            run = true
        }

        assert(run)
    }

    @Test fun `Strings are sent properly` () {
        var run: Boolean

        client.post("/string", "text").apply {
            assert(headers["body"]?.isNotEmpty() ?: false)
            assert(status == 200)
            run = true
        }

        assert(run)
    }

    @Test fun `Request HTTPS example`() {

        val serverAdapter = JettyServletAdapter()

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
        val client = Client(adapter, "https://localhost:${server.runtimePort}", clientSettings)
        client.get("/hello").apply {
            logger.debug { body }
            // Assure the certificate received (and returned) by the server is correct
            assert(headers.require("cert").first().startsWith("CN=hexagonkt.com"))
            assert(body == "Hello World!")
        }

        server.stop()
    }

    private fun checkResponse(
        response: Response, parameter: Map<String, String>?, format: SerializationFormat = Json) {

        assert(response.status == 200)
        assert(response.body?.trim() == parameter?.serialize(format)?.trim() ?: "")
    }
}
