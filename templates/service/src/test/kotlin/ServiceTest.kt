
import co.there4.hexagon.serialization.parse
import co.there4.hexagon.web.Client
import co.there4.hexagon.web.server
import org.asynchttpclient.Response
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

class BenchmarkTest {
    private val client by lazy { Client("http://localhost:${server.runtimePort}") }

    @BeforeClass fun warmup() {
        main(arrayOf())
    }

    fun json() {
        val response = client.get("/json")
        val content = response.responseBody

        checkResponse(response, "application/json")
        assert("Hello, World!" == content.parse(Message::class).message)
    }
}
