package com.hexagonkt.http.client.jetty

import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.model.HttpClientRequest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class JettyClientAdapterTest {

    @Test fun `Shut down not started client fails`() {
        val adapter = JettyClientAdapter()
        val message = assertFailsWith<IllegalStateException> { adapter.shutDown() }.message
        assertEquals("'null' Jetty HTTP client: Client *MUST BE STARTED* before shut-down", message)
    }

    @Test fun `Send request without starting client`() = runBlocking {
        val client = HttpClient(JettyClientAdapter())
        val request = HttpClientRequest()
        val message = assertFailsWith<IllegalStateException> { client.send(request) }.message
        val expectedMessage = "'null' HTTP client: Client *MUST BE STARTED* before sending requests"
        assertEquals(expectedMessage, message)
    }
}
