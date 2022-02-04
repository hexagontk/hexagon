package com.hexagonkt.http.server.netty

import com.hexagonkt.http.server.HttpServer
import io.netty.buffer.Unpooled
import io.netty.buffer.Unpooled.EMPTY_BUFFER
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.netty.handler.codec.http.HttpVersion.HTTP_1_1
import io.netty.util.CharsetUtil
import java.util.*

internal class NettyServerHandler(
    private val server: HttpServer
) : SimpleChannelInboundHandler<HttpObject>() {

    private val responseData = StringBuilder()
    private var keepAlive = true

    override fun channelReadComplete(context: ChannelHandlerContext) {
        context.flush()
    }

    override fun channelRead0(context: ChannelHandlerContext, message: HttpObject) {
        val result = message.decoderResult()
        if (!result.isSuccess) {
            responseData.append("..Decoder Failure: ")
            responseData.append(result.cause())
            responseData.append("\r\n")
        }

        if (message is HttpRequest) {
            responseData.append(RequestUtils.formatParams(message))
            keepAlive = HttpUtil.isKeepAlive(message)
        }

        if (message is HttpContent) {
            responseData.append(RequestUtils.formatBody(message))

            if (message is LastHttpContent) {
                responseData.append(RequestUtils.prepareLastResponse(message))
                writeResponse(context, message)
            }
        }
    }

    override fun exceptionCaught(context: ChannelHandlerContext, cause: Throwable) {
        context.close()
    }

    private fun writeResponse(context: ChannelHandlerContext, trailer: LastHttpContent) {

        val httpResponse: FullHttpResponse = DefaultFullHttpResponse(
            HTTP_1_1,
            if ((trailer as HttpObject).decoderResult().isSuccess) OK else BAD_REQUEST,
            Unpooled.copiedBuffer(responseData.toString(), CharsetUtil.UTF_8)
        )
        httpResponse.headers()[HttpHeaderNames.CONTENT_TYPE] = "text/plain; charset=UTF-8"
        if (keepAlive) {
            httpResponse.headers()
                .setInt(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes())
            httpResponse.headers()[HttpHeaderNames.CONNECTION] = HttpHeaderValues.KEEP_ALIVE
        }
        context.write(httpResponse)
        if (!keepAlive) {
            context.writeAndFlush(EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
        }
    }
}

internal object RequestUtils {
    fun formatParams(request: HttpRequest): StringBuilder {
        val responseData = StringBuilder()
        val queryStringDecoder = QueryStringDecoder(request.uri())
        val params = queryStringDecoder.parameters()
        if (params.isNotEmpty()) {
            for ((k, v) in params) {
                for (`val` in v) {
                    responseData.append("Parameter: ")
                        .append(k.uppercase(Locale.getDefault()))
                        .append(" = ")
                        .append(`val`.uppercase(Locale.getDefault()))
                        .append("\r\n")
                }
            }
            responseData.append("\r\n")
        }
        return responseData
    }

    fun formatBody(httpContent: HttpContent): StringBuilder {
        val responseData = StringBuilder()
        val content = httpContent.content()
        if (content.isReadable) {
            responseData.append(
                content.toString(CharsetUtil.UTF_8)
                    .uppercase(Locale.getDefault())
            )
            responseData.append("\r\n")
        }
        return responseData
    }

    fun prepareLastResponse(trailer: LastHttpContent): StringBuilder {

        val responseData = StringBuilder()
        responseData.append("Good Bye!\r\n")

        if (!trailer.trailingHeaders().isEmpty) {
            responseData.append("\r\n")
            for (name in trailer.trailingHeaders()
                .names()) {
                for (value in trailer.trailingHeaders()
                    .getAll(name)) {
                    responseData.append("P.S. Trailing Header: ")
                    responseData.append(name)
                        .append(" = ")
                        .append(value)
                        .append("\r\n")
                }
            }
            responseData.append("\r\n")
        }

        return responseData
    }
}
