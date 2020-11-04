package com.hexagonkt

import org.junit.jupiter.api.Test
import java.net.MalformedURLException
import java.net.URL
import kotlin.test.assertFailsWith

class ClasspathHandlerProviderTest {

    @Test fun `Require classpath resource`() {
        val resource = URL("classpath:application_test.yml")
        assert(resource.file == resource.file)
        val e = assertFailsWith<ResourceNotFoundException> {
            URL("classpath:foo.txt").openConnection()
        }
        assert(e.message == "classpath:foo.txt cannot be open")
    }

    @Test fun `Classpath resource folder`() {
        assert(URL("classpath:data").readText().lines().isNotEmpty())
    }

    @Test fun `Read classpath resource returns resource's text` () {
        val resourceText = URL("classpath:logback-test.xml").readText()
        assert(resourceText.contains("Logback configuration for tests"))
    }

    @Test fun `Unknown protocol throws exception`() {
        val e = assertFailsWith<MalformedURLException> {
            assert(URL("unknown:data").readText().lines().isNotEmpty())
        }
        assert(e.message == "unknown protocol: unknown")
    }

    @Test fun `Resource loading using URL returns data`() {
        assert(URL("classpath:data/companies.json").readText().isNotBlank())
        assert(URL("file:README.md").readText().isNotBlank())
        assert(URL("https://hexagonkt.com/index.html").readText().isNotBlank())
    }
}
