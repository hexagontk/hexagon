package com.hexagonkt.core

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class CheatSheetTest {

    @Test fun `Data utilities`() {
        // data
        /*
         * Utilities to ease the processing of collections, sets, and maps to ease mapping to
         * classes.
         */
        val map = mapOf(
            "map" to mapOf(
                "list" to listOf(10, 20, 30),
                "key" to "value",
            ),
        )

        // Retrieve nested map elements
        assertEquals(10, map.getPath("map", "list", 0))
        assertEquals(20, map.getPath("map", "list", 1))
        assertEquals("value", map.getPath("map", "key"))

        // data
    }

    @Test fun `Exceptions utilities`() {
        // exceptions
        // exceptions
    }

    @Test fun `Network utilities`() {
        // network
        // network
    }

    @Test fun `Strings utilities`() {
        // strings
        // strings

        // ansi
        // ansi
    }

    @Test fun `Classpath utilities`() {
        // classpath
        // classpath

        // resourceNotFound
        // resourceNotFound
    }

    @Test fun `Glob utilities`() {
        // glob
        // glob
    }

    @Test fun `Jvm utilities`() {
        // jvm
        // jvm
    }

    @Test fun `Logging utilities`() {
        // mediaTypes
        // mediaTypes
    }

    @Test fun `Media types utilities`() {
        // mediaTypes
        // mediaTypes
    }
}
