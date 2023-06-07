package com.hexagonkt.http.model

import com.hexagonkt.http.checkHeaders
import com.hexagonkt.http.model.ws.WsSession

data class HttpResponse(
    override val body: Any = "",
    override val headers: Headers = Headers(),
    override val contentType: ContentType? = null,
    override val cookies: List<Cookie> = emptyList(),
    override val status: HttpStatus = NOT_FOUND_404,
    override val contentLength: Long = -1L,
    override val onConnect: WsSession.() -> Unit = {},
    override val onBinary: WsSession.(data: ByteArray) -> Unit = {},
    override val onText: WsSession.(text: String) -> Unit = {},
    override val onPing: WsSession.(data: ByteArray) -> Unit = {},
    override val onPong: WsSession.(data: ByteArray) -> Unit = {},
    override val onClose: WsSession.(status: Int, reason: String) -> Unit = { _, _ -> },
) : HttpResponsePort {

    init {
        checkHeaders(headers)
    }

    override fun with(
        status: HttpStatus,
        body: Any,
        headers: Headers,
        contentType: ContentType?,
        cookies: List<Cookie>,
        onConnect: WsSession.() -> Unit,
        onBinary: WsSession.(data: ByteArray) -> Unit,
        onText: WsSession.(text: String) -> Unit,
        onPing: WsSession.(data: ByteArray) -> Unit,
        onPong: WsSession.(data: ByteArray) -> Unit,
        onClose: WsSession.(status: Int, reason: String) -> Unit,
    ): HttpResponsePort =
        copy(
            status = status,
            body = body,
            headers = headers,
            contentType = contentType,
            cookies = cookies,
            onConnect = onConnect,
            onBinary = onBinary,
            onText = onText,
            onPing = onPing,
            onPong = onPong,
            onClose = onClose,
        )
}
