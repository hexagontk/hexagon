package com.hexagonkt.http.server.netty

import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.model.HttpServerResponse
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener.CLOSE
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpHeaderNames.*
import io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE
import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.netty.handler.codec.http.HttpVersion.HTTP_1_1
import io.netty.util.CharsetUtil.UTF_8

internal class NettyServerHandler(
    private val handlers: Map<HttpMethod, PathHandler>
) : SimpleChannelInboundHandler<FullHttpRequest>() {

    lateinit var httpRequest: FullHttpRequest

    @Suppress("deprecation") // Deprecated in ChannelHandler, not in SimpleChannelInboundHandler
    override fun channelRead0(context: ChannelHandlerContext, request: FullHttpRequest) {
        httpRequest = request
        val result = request.decoderResult()
        if (result.isFailure)
            exceptionCaught(context, result.cause())

        val method = request.method
        val response = handlers[method]
            ?.process(NettyRequestAdapter(method, request))
            ?: HttpServerResponse()

        val data =
            RequestUtils.formatParams(request)
                .append(RequestUtils.formatBody(request))
                .append(RequestUtils.prepareLastResponse(request))
                .toString()

        writeResponse(context, data, OK)
    }

    override fun exceptionCaught(context: ChannelHandlerContext, cause: Throwable) {
        writeResponse(context, "Failure: $cause\n", BAD_REQUEST)
    }

    private fun writeResponse(
        context: ChannelHandlerContext,
        data: String,
        status: HttpResponseStatus,
    ) {

        val buffer = Unpooled.copiedBuffer(data, UTF_8)
        val response = DefaultFullHttpResponse(HTTP_1_1, status, buffer)
        val headers = response.headers()

        headers[CONTENT_TYPE] = "text/plain; charset=UTF-8"

        if (HttpUtil.isKeepAlive(httpRequest)) {
            headers.setInt(CONTENT_LENGTH, response.content().readableBytes())
            headers[CONNECTION] = KEEP_ALIVE
            context.writeAndFlush(response)
        }
        else {
            context.writeAndFlush(response).addListener(CLOSE)
        }
    }
}

internal object RequestUtils {

    fun formatParams(request: HttpRequest): StringBuilder {
        val responseData = StringBuilder()
        val queryStringDecoder = QueryStringDecoder(request.uri())
        val params = queryStringDecoder.parameters()

        if (params.isNotEmpty())
            for ((k, vs) in params)
                for (v in vs)
                    responseData.append("Parameter: ${k.uppercase()} = ${v.uppercase()}\n")

        return responseData
    }

    fun formatBody(httpContent: HttpContent): StringBuilder {
        val responseData = StringBuilder()
        val content = httpContent.content()

        if (content.isReadable)
            responseData.append(content.toString(UTF_8).uppercase())

        return responseData
    }

    fun prepareLastResponse(trailer: LastHttpContent): StringBuilder {
        val responseData = StringBuilder()
        responseData.append("Good Bye!")

        if (!trailer.trailingHeaders().isEmpty)
            for (name in trailer.trailingHeaders().names())
                for (value in trailer.trailingHeaders().getAll(name))
                    responseData.append("P.S. Trailing Header: $name = $value\n")

        return responseData
    }
}
