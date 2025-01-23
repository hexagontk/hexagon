package com.hexagontk.http.model

import com.hexagontk.http.model.ws.WsSession

// TODO Handle contentType, accept, authorization, etc. as headers instead of actual fields
class HttpResponse(
    override val body: Any = "",
    override val headers: Headers = Headers(),
    override val contentType: ContentType? = null,
    override val cookies: List<Cookie> = emptyList(),
    override val status: Int = NOT_FOUND_404,
    override val reason: String = "",
    override val contentLength: Long = -1L,
    override val onConnect: WsSession.() -> Unit = {},
    override val onBinary: WsSession.(data: ByteArray) -> Unit = {},
    override val onText: WsSession.(text: String) -> Unit = {},
    override val onPing: WsSession.(data: ByteArray) -> Unit = {},
    override val onPong: WsSession.(data: ByteArray) -> Unit = {},
    override val onClose: WsSession.(status: Int, reason: String) -> Unit = { _, _ -> },
) : HttpResponsePort {

    override fun with(
        status: Int,
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
        HttpResponse(
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
