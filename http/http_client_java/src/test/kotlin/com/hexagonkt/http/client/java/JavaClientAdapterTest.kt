package com.hexagonkt.http.client.java

import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.model.HttpRequest
import org.junit.jupiter.api.Test
import kotlin.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class JavaClientAdapterTest {

    @Test fun `Send request without starting client`() {
        val client = HttpClient(JavaClientAdapter())
        val request = HttpRequest()
        val message = assertFailsWith<IllegalStateException> { client.send(request) }.message
        val expectedMessage = "HTTP client *MUST BE STARTED* before sending requests"
        assertEquals(expectedMessage, message)
    }
}
