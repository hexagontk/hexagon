package com.hexagonkt.http.client.jetty

import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.model.ws.WsSession
import org.eclipse.jetty.websocket.client.WebSocketClient
import java.net.URI
import java.net.URL

/**
 * Client to use other REST services.
 */
class JettyWsClientAdapter : JettyClientAdapter() {

    private lateinit var wsClient: WebSocketClient

    override fun startUp(client: HttpClient) {
        super.startUp(client)
        wsClient = WebSocketClient(jettyClient)
        wsClient.start()
    }

    override fun shutDown() {
        super.shutDown()
        wsClient.stop()
    }

    override fun ws(
        path: String,
        onConnect: WsSession.() -> Unit,
        onBinary: WsSession.(data: ByteArray) -> Unit,
        onText: WsSession.(text: String) -> Unit,
        onPing: WsSession.(data: ByteArray) -> Unit,
        onPong: WsSession.(data: ByteArray) -> Unit,
        onClose: WsSession.(status: Int, reason: String) -> Unit,
    ): WsSession {

        val baseUrl = httpClient.settings.baseUrl ?: URL(path)
        val scheme = if (baseUrl.protocol.lowercase() == "https") "wss" else "ws"
        val uri = URI("$scheme://${baseUrl.host}:${baseUrl.port}${baseUrl.path}$path")
        val adapter =
            JettyWebSocketAdapter(uri, onConnect, onBinary, onText, onPing, onPong, onClose)
        val session = wsClient.connect(adapter, uri).get()

        return JettyClientWsSession(uri, session)
    }
}
