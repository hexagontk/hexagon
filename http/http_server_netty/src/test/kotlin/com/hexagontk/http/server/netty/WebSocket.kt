package com.hexagontk.http.server.netty

import com.hexagontk.core.debug
import com.hexagontk.core.logger
import com.hexagontk.core.urlOf
import com.hexagontk.http.model.ServerEvent
import com.hexagontk.http.server.HttpServer
import com.hexagontk.http.server.callbacks.UrlCallback
import com.hexagontk.http.model.ws.WsSession
import java.util.concurrent.*
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.function.Supplier

internal lateinit var server: HttpServer

internal class PeriodicPublisher<T>(
    executor: Executor,
    maxBufferCapacity: Int,
    period: Long,
    unit: TimeUnit,
    supplier: Supplier<out T>,
) : SubmissionPublisher<T>(executor, maxBufferCapacity) {

    private val scheduler: ScheduledExecutorService = ScheduledThreadPoolExecutor(1)
    private val periodicTask: ScheduledFuture<*> = scheduler.scheduleAtFixedRate(
        {
            val get = supplier.get()
            submit(get)
        },
        0,
        period,
        unit,
    )

    override fun close() {
        periodicTask.cancel(false)
        scheduler.shutdown()
        super.close()
    }
}

fun event(
    executor: Executor,
    period: Long,
    supplier: Supplier<ServerEvent>
): SubmissionPublisher<ServerEvent> =
    PeriodicPublisher(executor, 10, period, MILLISECONDS, supplier)

fun main () {
    val executor = Executors.newSingleThreadExecutor()
    var sessions = emptyList<WsSession>()

    server = serve {
        get(callback = UrlCallback(urlOf("classpath:ws.html")))
        get("/sse") {
            val body = event(executor, 2_000) {
                ServerEvent(data = System.currentTimeMillis().toString())
            }
            sse(body)
        }

        ws("/ws") {
            accepted(
                onConnect = {
                    sessions = sessions + this
                },

                onBinary = {
                    val text = String(it)
                    logger.debug { "BinaryWebSocketFrame Received : $text" }
                },

                onText = {
                    for (s in sessions)
                        s.send(it)
                    logger.debug { "TextWebSocketFrame Received : $it" }
                },

                onPing = {
                    val text = String(it)
                    logger.debug { "PingWebSocketFrame Received : $text" }
                },

                onPong= {
                    val text = String(it)
                    logger.debug { "PongWebSocketFrame Received : $text" }
                },

                onClose = { statusCode, reason ->
                    logger.debug { "CloseWebSocketFrame. Reason: $reason, Status : $statusCode" }
                }
            )
        }
    }
}
