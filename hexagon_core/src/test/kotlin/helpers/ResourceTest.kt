package com.hexagonkt.helpers

import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class ResourceTest {

    @Test fun `Require resource`() {
        val resource = Resource("service_test.yaml")
        assert(resource.requireUrl().file == resource.url()?.file)
        assertFailsWith<IllegalStateException>("foo.txt not found") {
            Resource("foo.txt").requireUrl()
        }
        assertFailsWith<IllegalStateException>("foo.txt not found") {
            Resource("foo.txt").requireStream()
        }
    }

    @Test fun `Resource folder`() {
        assert(Resource("data").url()?.readText()?.lines()?.size ?: 0 > 0)
    }

    @Test fun `readResource returns resource's text` () {
        val resourceText = Resource("logback-test.xml").readText()
        assert(resourceText?.contains("Logback configuration for tests") ?:false)
    }
}
