package com.hexagonkt.http.client.netty

import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.model.HttpRequest
import org.junit.jupiter.api.Test
import kotlin.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class NettyClientAdapterTest {

    @Test fun `Send request without starting client`() {
        val client = HttpClient(NettyClientAdapter())
        val request = HttpRequest()
        val message = assertFailsWith<IllegalStateException> { client.send(request) }.message
        val expectedMessage = "HTTP client *MUST BE STARTED* before sending requests"
        assertEquals(expectedMessage, message)
    }
}
