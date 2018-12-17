package com.hexagonkt.http

import com.hexagonkt.http.HttpMethod.GET
import org.testng.annotations.Test

@Test class RouteTest {

    fun `Route constructor works ok`() {
        val metadata = mapOf("doc" to "text")
        val route = Route(Path("/"), linkedSetOf(GET), String::class, Int::class, metadata)
        assert(route.requestType == String::class)
        assert(route.responseType == Int::class)
        assert(route.metadata == metadata)
    }
}
