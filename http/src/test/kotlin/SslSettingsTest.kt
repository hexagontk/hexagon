package com.hexagonkt.http

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

internal class SslSettingsTest {

    @Test fun `Default SSL settings contains the proper values`() {
        SslSettings().let {
            assertNull(it.keyStore)
            assertEquals("", it.keyStorePassword)
            assertNull(it.trustStore)
            assertEquals("", it.trustStorePassword)
            assertFalse(it.clientAuth)
        }
    }
}
