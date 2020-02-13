
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.client.ahc.AhcAdapter
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * Required because SonarQube doesn't seem to take into account JUnit tests. Ignored for archetype.
 */
class ServiceTestNgTest {
    private val client: Client by lazy {
        Client(AhcAdapter(), "http://localhost:${server.runtimePort}")
    }

    @BeforeClass fun startup() {
        main()
    }

    @AfterClass fun shutdown() {
        server.stop()
    }

    @Test fun `HTTP request returns the correct body`() {
        val response = client.get("/hello/World")
        val content = response.body

        assert(response.headers["Date"] != null)
        assert(response.headers["Content-Type"]?.first() == "text/plain")

        assert("Hello, World!" == content)
    }
}
