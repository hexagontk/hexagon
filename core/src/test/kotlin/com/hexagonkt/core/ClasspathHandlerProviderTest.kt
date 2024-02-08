package com.hexagonkt.core

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.net.MalformedURLException
import kotlin.test.assertFailsWith

@TestInstance(PER_CLASS)
internal class ClasspathHandlerProviderTest {

    @BeforeAll fun registerHandler() {
        ClasspathHandler.registerHandler()
    }

    @Test fun `Classpath utilities`() {
        // classpath
        // TODO
        // classpath

        // resourceNotFound
        // TODO
        // resourceNotFound
    }

    @Test fun `Registering classpath handler twice does not fail`() {
        ClasspathHandler.registerHandler()
    }

    @Test fun `Require classpath resource`() {
        val resource = urlOf("classpath:application_test.yml")
        assert(resource.file == resource.file)
        val e = assertFailsWith<ResourceNotFoundException> {
            urlOf("classpath:foo.txt").openConnection()
        }
        assert(e.message == "classpath:foo.txt cannot be open")
    }

    @Test fun `Classpath resource folder`() {
        assert(urlOf("classpath:data").readText().lines().isNotEmpty())
    }

    @Test fun `Read classpath resource returns resource's text` () {
        val resourceText = urlOf("classpath:sample.properties").readText()
        assert(resourceText.contains("handlers=java.util.logging.ConsoleHandler"))
    }

    @Test fun `Unknown protocol throws exception`() {
        val e = assertFailsWith<MalformedURLException> {
            assert(urlOf("unknown:data").readText().lines().isNotEmpty())
        }

        val errorJvm = "unknown protocol: unknown"
        val errorNative = "The URL protocol unknown is not tested"
        val message = e.message ?: "_"
        assert(message.contains(errorJvm) || message.contains(errorNative))
    }

    @Test fun `Resource loading using URL returns data`() {
        assert(urlOf("classpath:data/companies.json").readText().isNotBlank())
        assert(urlOf("file:README.md").readText().isNotBlank())
    }
}
