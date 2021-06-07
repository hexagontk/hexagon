@file:Suppress("ClassName", "unused")

package com.hexagonkt.helpers

import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.Yaml
import org.junit.jupiter.api.Test
import java.net.URL
import java.util.*
import kotlin.test.assertEquals

internal class SampleBundle : UrlResourceBundle(URL("classpath:sample.yml"))
internal class SampleBundle_es : UrlResourceBundle(URL("classpath:sample_es.yml"))

internal class UrlResourceBundleTest {

    @Test fun `UrlResourceBundle loads parameters from classpath`() {
        SerializationManager.formats = linkedSetOf(Yaml)

        resourceBundle<SampleBundle>(Locale("es", "ES")).let {
            assertEquals("Nombre", it.getObject("name"))
            assertEquals("Dirección", it.getObject("address"))
            assertEquals("Age", it.getObject("age"))
        }

        resourceBundle<SampleBundle>(Locale("es")).let {
            assertEquals("Nombre", it.getObject("name"))
            assertEquals("Dirección", it.getObject("address"))
            assertEquals("Age", it.getObject("age"))
        }

        resourceBundle<SampleBundle>(Locale("en", "US")).let {
            assertEquals("Name", it.getObject("name"))
            assertEquals("Address", it.getObject("address"))
            assertEquals("Age", it.getObject("age"))
        }
    }
}
