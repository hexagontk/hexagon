package com.hexagonkt.http

import com.hexagonkt.http.Method.GET
import org.junit.jupiter.api.Test

internal class RouteTest {

    @Test fun `Route constructor works ok`() {
        val route = Route(Path("/"), linkedSetOf(GET))
        assert(route.path.pattern == "/")
        assert(route.methods == setOf(GET))
    }
}
