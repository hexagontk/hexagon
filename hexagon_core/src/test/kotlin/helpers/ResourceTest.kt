package com.hexagonkt.helpers

import org.junit.jupiter.api.Test
import java.net.MalformedURLException
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

    @Test fun `Unknown protocol throws exception`() {
        val e = assertFailsWith<MalformedURLException> {
            assert(URL("unknown:data").readText().lines().isNotEmpty())
        }
        assert(e.message == "unknown protocol: unknown")
    }

    @Test fun `Resource loading using URL`() {
        assert(URL("classpath:application_test.yml").readText().isNotBlank())
        assert(URL("file:README.md").readText().isNotBlank())
        assert(URL("https://hexagonkt.com/index.html").readText().isNotBlank())
    }
}
