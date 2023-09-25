package com.hexagonkt.http.handlers

import com.hexagonkt.core.require
import com.hexagonkt.http.model.Header
import com.hexagonkt.http.model.HttpMethod.GET
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class RoutingTest {

    private val path: PathHandler = path {
        before("*") {
            send(headers = response.headers + Header("before", "true"))
        }

        path("/after") {
            get("/resource") { ok("resource") }
            get("/{var:\\d+}") { ok("var:regex") }
            get("/{var}") { ok("var") }
        }
    }

    @Test fun `On handlers stop being processed after first match`() {
        assertEquals("true", path.send(GET, "/after/resource").headers.require("before").string())
        assertEquals("true", path.send(GET, "/after/123").headers.require("before").string())
        assertEquals("true", path.send(GET, "/after/abc").headers.require("before").string())

        assertEquals("resource", path.send(GET, "/after/resource").bodyString())
        assertEquals("var:regex", path.send(GET, "/after/123").bodyString())
        assertEquals("var", path.send(GET, "/after/abc").bodyString())
    }
}
