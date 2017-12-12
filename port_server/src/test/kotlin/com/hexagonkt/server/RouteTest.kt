package com.hexagonkt.server

import com.hexagonkt.HttpMethod.GET
import org.testng.annotations.Test

@Test class RouteTest {
    fun route_constructor() {
        val metadata = mapOf("doc" to "text")
        val route = Route(Path("/"), linkedSetOf(GET), String::class, Int::class, metadata)
        assert(route.requestType == String::class)
        assert(route.responseType == Int::class)
        assert(route.metadata == metadata)
    }
}
