package com.hexagonkt.http.server.netty

import com.hexagonkt.http.server.HttpServer
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
    private val server: HttpServer
) : SimpleChannelInboundHandler<HttpObject>() {

    private val responseData = StringBuilder()
    private var keepAlive = true

    override fun channelReadComplete(context: ChannelHandlerContext) {
        context.flush()
    }

    @Suppress("deprecation") // Deprecated in ChannelHandler, not in SimpleChannelInboundHandler
    override fun channelRead0(context: ChannelHandlerContext, message: HttpObject) {
        val result = message.decoderResult()
        if (result.isFailure)
            exceptionCaught(context, result.cause())

        if (message is HttpRequest) {
            responseData.append(RequestUtils.formatParams(message))
            keepAlive = HttpUtil.isKeepAlive(message) // TODO Is this required every read?
        }

        if (message is HttpContent) {
            responseData.append(RequestUtils.formatBody(message))

            if (message is LastHttpContent) {
                responseData.append(RequestUtils.prepareLastResponse(message))
                writeResponse(context, OK)
            }
        }
    }

    override fun exceptionCaught(context: ChannelHandlerContext, cause: Throwable) {
        responseData.append("Failure: $cause\n")
        writeResponse(context, BAD_REQUEST)
    }

    private fun writeResponse(context: ChannelHandlerContext, status: HttpResponseStatus) {
        val buffer = Unpooled.copiedBuffer(responseData.toString(), UTF_8)
        val response = DefaultFullHttpResponse(HTTP_1_1, status, buffer)
        val headers = response.headers()

        headers[CONTENT_TYPE] = "text/plain; charset=UTF-8"

        if (keepAlive) {
            headers.setInt(CONTENT_LENGTH, response.content().readableBytes())
            headers[CONNECTION] = KEEP_ALIVE
            context.write(response)
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
