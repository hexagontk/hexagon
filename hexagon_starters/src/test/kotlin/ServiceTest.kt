
import com.hexagonkt.client.Client
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

@Test class ServiceTest {
    private val client by lazy { Client("http://localhost:${server.runtimePort}") }

    @BeforeClass fun startup() {
        main()
    }

    @AfterClass fun shutdown() {
        server.stop()
    }

    @Test fun httpRequest() {
        val response = client.get("/text")
        val content = response.responseBody

        assert(response.headers ["Date"] != null)
        assert(response.headers ["Server"] != null)
        assert(response.headers ["Transfer-Encoding"] != null)
        assert(response.headers ["Content-Type"] == "text/plain")

        assert("Hello, World!" == content)
    }
}
