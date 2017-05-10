package co.there4.hexagon.server

import co.there4.hexagon.server.HttpMethod.GET
import org.testng.annotations.Test

@Test class RouteTest {
    fun route_constructor() {
        val route = Route(Path("/"), setOf(GET), String::class, Int::class, mapOf("doc" to "text"))
        assert(route.requestType == String::class)
        assert(route.responseType == Int::class)
        assert(route.metadata == mapOf("doc" to "text"))
    }
}
