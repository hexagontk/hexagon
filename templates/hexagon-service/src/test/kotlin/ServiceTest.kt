package ${group}

import co.there4.hexagon.web.Client
import co.there4.hexagon.web.server
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

@Test class ${className}Test {
    private val client by lazy { Client("http://localhost:\${server.runtimePort}") }

    @BeforeClass fun warmup() {
        main(arrayOf())
    }

    fun json() {
        val response = client.get("/text")
        val content = response.responseBody

        assert(response.headers ["Date"] != null)
        assert(response.headers ["Server"] != null)
        assert(response.headers ["Transfer-Encoding"] != null)
        assert(response.headers ["Content-Type"] == "application/json")

        assert("Hello, World!" == content)
    }
}
