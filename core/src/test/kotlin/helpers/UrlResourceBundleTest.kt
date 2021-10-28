package com.hexagonkt.helpers

import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.yaml.Yaml
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

internal class UrlResourceBundleTest {

    @Test fun `UrlResourceBundle loads parameters from classpath`() {
        SerializationManager.formats = linkedSetOf(Yaml)

        resourceBundle<SampleBundle>(Locale("es", "ES")).let {
            assertEquals("classpath:sample_es.yml", (it as? SampleBundle_es)?.url.toString())
            assertEquals("Nombre", it.getObject("name"))
            assertEquals("Dirección", it.getObject("address"))
            assertEquals("Age", it.getObject("age"))
        }

        resourceBundle<SampleBundle>(Locale("es")).let {
            assertEquals("classpath:sample_es.yml", (it as? SampleBundle_es)?.url.toString())
            assertEquals("Nombre", it.getObject("name"))
            assertEquals("Dirección", it.getObject("address"))
            assertEquals("Age", it.getObject("age"))
        }

        resourceBundle<SampleBundle>(Locale("en", "US")).let {
            assertEquals("classpath:sample.yml", (it as? SampleBundle)?.url.toString())
            assertEquals("Name", it.getObject("name"))
            assertEquals("Address", it.getObject("address"))
            assertEquals("Age", it.getObject("age"))
        }
    }
}
