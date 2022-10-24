package com.hexagonkt.http.server.model

import com.hexagonkt.http.checkHeaders
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.ClientErrorStatus.NOT_FOUND

data class HttpServerResponse(
    override val body: Any = "",
    override val headers: HttpFields<Header> = HttpFields(),
    override val contentType: ContentType? = null,
    override val cookies: List<HttpCookie> = emptyList(),
    override val status: HttpStatus = NOT_FOUND,
    val onConnect: WsSession.() -> Unit = {},
    val onBinary: WsSession.(data: ByteArray) -> Unit = {},
    val onText: WsSession.(text: String) -> Unit = {},
    val onPing: WsSession.(data: ByteArray) -> Unit = {},
    val onPong: WsSession.(data: ByteArray) -> Unit = {},
    val onClose: WsSession.(statusCode: Int, reason: String) -> Unit = { _, _ -> },
) : HttpResponse {

    init {
        checkHeaders(headers)
    }
}
