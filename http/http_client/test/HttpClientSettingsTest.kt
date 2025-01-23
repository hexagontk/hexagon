package com.hexagontk.http.client

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class HttpClientSettingsTest {

    @Test fun `Default HTTP client settings contains the proper values`() {
        HttpClientSettings().let {
            assertNull(it.baseUri)
            assertNull(it.contentType)
            assertEquals(emptyList(), it.accept)
            assertTrue(it.useCookies)
            assertTrue(it.headers.fields.isEmpty())
            assertFalse(it.insecure)
            assertNull(it.sslSettings)
            assertFalse(it.followRedirects)
        }
    }
}
