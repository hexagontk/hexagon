package co.there4.hexagon.rest

import org.testng.annotations.Test
import java.net.URL

@Test class RestPackageTest {
    fun rest_application_starts_correctly () {
        appStart {
            handlers {
                get ("hi") { render ("Hello World") }
            }
        }

        val client = HttpClient (URL ("http://localhost:5050"))
        assert (client.get("/hi") == "Hello World")
    }
}
