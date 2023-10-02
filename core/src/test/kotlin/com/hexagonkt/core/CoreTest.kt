package com.hexagonkt.core

import org.junit.jupiter.api.Test
import kotlin.test.assertContains

internal class CoreTest {

    @Test fun buildPropertiesBundled() {
        val hexagonProperties = urlOf("classpath:hexagon.properties").readText()

        assertContains(hexagonProperties, "project=")
        assertContains(hexagonProperties, "module=")
        assertContains(hexagonProperties, "version=")
        assertContains(hexagonProperties, "group=")
        assertContains(hexagonProperties, "description=")
    }
}
