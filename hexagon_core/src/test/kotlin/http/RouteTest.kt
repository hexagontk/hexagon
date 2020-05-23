package com.hexagonkt.http

import com.hexagonkt.http.Method.GET
import org.junit.jupiter.api.Test

class RouteTest {

    @Test fun `Route constructor works ok`() {
        val metadata = mapOf("doc" to "text")
        val route = Route(Path("/"), linkedSetOf(GET), String::class, Int::class, metadata)
        assert(route.requestType == String::class)
        assert(route.responseType == Int::class)
        assert(route.metadata == metadata)
    }
}
