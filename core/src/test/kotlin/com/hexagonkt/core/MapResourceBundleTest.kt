package com.hexagonkt.core

import kotlin.test.Test
import java.util.*
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
