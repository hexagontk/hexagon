package com.hexagonkt.http.client

import com.hexagonkt.http.model.Headers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class HttpClientSettingsTest {

    @Test fun `Default HTTP client settings contains the proper values`() {
        HttpClientSettings().let {
            assertNull(it.baseUrl)
            assertNull(it.contentType)
            assertEquals(emptyList(), it.accept)
            assertTrue(it.useCookies)
            assertEquals(Headers(), it.headers)
            assertFalse(it.insecure)
            assertNull(it.sslSettings)
            assertFalse(it.followRedirects)
        }
    }
}
