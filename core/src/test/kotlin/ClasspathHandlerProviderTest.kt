package com.hexagonkt.core

import com.hexagonkt.core.logging.LoggingLevel
import com.hexagonkt.core.logging.LoggingManager
import com.hexagonkt.serialization.json.JacksonMapper
import com.hexagonkt.serialization.json.Json
import com.hexagonkt.core.serialization.SerializationManager
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.net.MalformedURLException
import java.net.URL
import kotlin.test.assertFailsWith

@TestInstance(PER_CLASS)
internal class ClasspathHandlerProviderTest {

    @BeforeAll fun registerHandler() {
        SerializationManager.formats = linkedSetOf(Json)
        SerializationManager.mapper = JacksonMapper
        LoggingManager.setLoggerLevel("com.hexagonkt", LoggingLevel.TRACE)
        ClasspathHandler.registerHandler()
    }

    @Test fun `Registering classpath handler twice does not fail`() {
        ClasspathHandler.registerHandler()
    }

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
        val resourceText = URL("classpath:logging.properties").readText()
        assert(resourceText.contains("handlers=com.hexagonkt.core.logging.jul.SystemOutHandler"))
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
    }
}
