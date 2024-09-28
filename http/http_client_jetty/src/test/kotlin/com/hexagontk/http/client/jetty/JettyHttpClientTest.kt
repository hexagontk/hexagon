package com.hexagontk.http.client.jetty

import com.hexagontk.http.client.HttpClient
import com.hexagontk.http.model.HttpRequest
import org.junit.jupiter.api.Test
import kotlin.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class JettyHttpClientTest {

    @Test fun `Send request without starting client`() {
        val client = HttpClient(JettyHttpClient())
        val request = HttpRequest()
        val message = assertFailsWith<IllegalStateException> { client.send(request) }.message
        val expectedMessage = "HTTP client *MUST BE STARTED* before sending requests"
        assertEquals(expectedMessage, message)
    }
}
