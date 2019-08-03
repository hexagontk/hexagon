
import com.hexagonkt.http.client.Client
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
class ServiceTest {
    private val client by lazy { Client("http://localhost:${server.runtimePort}") }

    @BeforeAll fun startup() {
        main()
    }

    @AfterAll fun shutdown() {
        server.stop()
    }

    @Test fun `HTTP request returns the correct body`() {
        val response = client.get("/hello/World")
        val content = response.responseBody

        assert(response.headers ["Date"] != null)
        assert(response.headers ["Content-Type"] == "text/plain")

        assert("Hello, World!" == content)
    }
}
