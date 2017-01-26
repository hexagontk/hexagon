package co.there4.hexagon.rest

import co.there4.hexagon.repository.Company
import co.there4.hexagon.repository.mongoIdRepository
import co.there4.hexagon.web.Client
import co.there4.hexagon.web.run
import co.there4.hexagon.web.server
import co.there4.hexagon.web.stop
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/** TODO Fix this test! */
@Test(enabled = false, description = "Only works in IDE") class RestCrudTest {
    val client: Client by lazy { Client("http://localhost:${server.runtimePort}") }

    @BeforeClass fun startup() {
        stop()
        crud(mongoIdRepository(Company::id), true)
        run()
    }

    @AfterClass fun shutdown() { stop() }

    fun read_only_crud() {
        assert(client.put("/${Company::class.simpleName}").statusCode == 405)
    }
}
