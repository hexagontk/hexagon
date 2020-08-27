package com.hexagonkt.helpers

import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.test.assertFailsWith

class ResourceTest {

    @Test fun `Require resource`() {
        val resource = URL("classpath:application_test.yml")
        assert(resource.file == resource.file)
        val e = assertFailsWith<IllegalStateException> {
            URL("classpath:foo.txt").openConnection()
        }
        assert(e.message == "foo.txt cannot be open")
    }

    @Test fun `Resource folder`() {
        assert(URL("classpath:data").readText().lines().isNotEmpty())
    }

    @Test fun `readResource returns resource's text` () {
        val resourceText = URL("classpath:logback-test.xml").readText()
        assert(resourceText.contains("Logback configuration for tests"))
    }
}
