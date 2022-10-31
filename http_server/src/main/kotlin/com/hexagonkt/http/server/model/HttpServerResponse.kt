package com.hexagonkt.http.server.model

import com.hexagonkt.http.checkHeaders
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.ClientErrorStatus.NOT_FOUND
import com.hexagonkt.http.model.ws.WsCloseStatus
import com.hexagonkt.http.server.model.ws.WsServerSession

data class HttpServerResponse(
    override val body: Any = "",
    override val headers: Headers = Headers(),
    override val contentType: ContentType? = null,
    override val cookies: List<Cookie> = emptyList(),
    override val status: HttpStatus = NOT_FOUND,
    val onConnect: WsServerSession.() -> Unit = {},
    val onBinary: WsServerSession.(data: ByteArray) -> Unit = {},
    val onText: WsServerSession.(text: String) -> Unit = {},
    val onPing: WsServerSession.(data: ByteArray) -> Unit = {},
    val onPong: WsServerSession.(data: ByteArray) -> Unit = {},
    val onClose: WsServerSession.(status: WsCloseStatus, reason: String) -> Unit = { _, _ -> },
) : HttpResponse {

    init {
        checkHeaders(headers)
    }
}
