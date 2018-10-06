package com.hexagonkt.helpers

import org.testng.annotations.Test
import kotlin.test.assertFailsWith

@Test class IoTest {

    @Test
    fun `Require resource`() {
        assert(requireResource("service_test.yaml").file == resource("service_test.yaml")?.file)
        assertFailsWith<IllegalStateException>("foo.txt not found") {
            requireResource("foo.txt")
        }
    }

    @Test fun `Resource folder`() {
        assert(resource("data")?.readText()?.lines()?.size ?: 0 > 0)
    }

    @Test fun `readResource returns resource's text` () {
        val resourceText = readResource("logback-test.xml")
        assert(resourceText?.contains("Logback configuration for tests") ?:false)
    }
}
