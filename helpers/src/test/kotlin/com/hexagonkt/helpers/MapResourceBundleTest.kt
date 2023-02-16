package com.hexagonkt.helpers

import kotlin.test.Test
import kotlin.test.assertEquals

internal class MapResourceBundleTest {

    internal class SampleBundle : MapResourceBundle(
        "age" to "Age",
        "name" to "Name",
        "address" to "Address",
    )

    @Suppress("ClassName", "unused") // Resolved internally, not used directly
    internal class SampleBundle_es : MapResourceBundle(
        "name" to "Nombre",
        "address" to "Dirección",
    )

    @Test fun `UrlResourceBundle loads parameters from classpath`() {

        resourceBundle<SampleBundle>(localeOf("es", "ES")).let {
            assertEquals("Nombre", it.getObject("name"))
            assertEquals("Dirección", it.getObject("address"))
            assertEquals("Age", it.getObject("age"))
        }

        resourceBundle<SampleBundle>(localeOf("es")).let {
            assertEquals("Nombre", it.getObject("name"))
            assertEquals("Dirección", it.getObject("address"))
            assertEquals("Age", it.getObject("age"))
        }

        resourceBundle<SampleBundle>(localeOf("en", "US")).let {
            assertEquals("Name", it.getObject("name"))
            assertEquals("Address", it.getObject("address"))
            assertEquals("Age", it.getObject("age"))
        }
    }
}
