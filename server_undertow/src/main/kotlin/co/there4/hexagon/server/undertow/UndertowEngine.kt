package co.there4.hexagon.server.undertow

import co.there4.hexagon.helpers.error
import co.there4.hexagon.helpers.CachedLogger
import co.there4.hexagon.helpers.CodedException
import co.there4.hexagon.server.*
import co.there4.hexagon.server.FilterOrder.AFTER
import co.there4.hexagon.server.FilterOrder.BEFORE
import co.there4.hexagon.server.RequestHandler.*
import io.undertow.Handlers
import io.undertow.Undertow
import io.undertow.Handlers.*
import io.undertow.server.handlers.BlockingHandler
import io.undertow.server.session.InMemorySessionManager
import io.undertow.server.session.SessionAttachmentHandler
import io.undertow.server.session.SessionCookieConfig
import java.net.InetSocketAddress
import java.net.InetAddress.getByName as address

class UndertowEngine : ServerEngine {
    companion object : CachedLogger(UndertowEngine::class)

    private var undertow: Undertow? = null
    private var started = false

    override fun started() = started

    override fun runtimePort(): Int =
        (undertow?.listenerInfo?.get(0)?.address as? InetSocketAddress)?.port ?: error

    fun build(server: Server) {
        val root = routing()

        val requestHandlers = server.router.flatRequestHandlers()
        val filtersByOrder = requestHandlers
            .filterIsInstance(FilterHandler::class.java)
            .groupBy { it.order }

        val beforeFilters = filtersByOrder[BEFORE] ?: listOf()
        val afterFilters = filtersByOrder[AFTER] ?: listOf()

        Handlers.pathTemplate()
        val codedErrors: Map<Int, ErrorCodeCallback> = requestHandlers
            .filterIsInstance(CodeHandler::class.java)
            .map { it.code to it.callback }
            .toMap()

        val exceptionErrors: Map<Class<out Exception>, ExceptionCallback> = requestHandlers
            .filterIsInstance(ExceptionHandler::class.java)
            .map { it.exception to it.callback }
            .toMap()

        beforeFilters.forEach {
            it.route.methods.forEach {

            }
        }

        for (handler in requestHandlers) {
            val route = handler.route

            when (handler) {
                is AssetsHandler -> {}
                is RouteHandler -> {
                    route.methods.forEach { m ->
                        root.add(m.toString (), route.path.path, BlockingHandler {
                            val undertowExchange = Call (
                                Request(UndertowRequest (it, route)),
                                Response(UndertowResponse (it)),
                                Session(UndertowSession (it))
                            )

                            try {
                                val handlerCallback = handler.callback
                                undertowExchange.handlerCallback()
                            }
                            catch (e: PassException) {
                                // Jumps to the next handler
                            }
                            catch (e: Exception) {
                                handleException (e, undertowExchange, codedErrors, exceptionErrors)
                            }
                            finally {
                                it.statusCode = undertowExchange.response.status
                                it.responseSender.send (undertowExchange.response.body.toString())
                                it.responseSender.close()
                            }
                        })
                    }
                }
                else -> warn("unhandled")
            }

        }

        val sessionHandler = SessionAttachmentHandler(
            InMemorySessionManager("session_manager"),
            SessionCookieConfig())

        sessionHandler.next = root

        val bindPort = server.bindPort
        val hostName = server.bindAddress.hostName
        val gracefulShutdown = gracefulShutdown(sessionHandler)
        val builder = Undertow.builder().addHttpListener(bindPort, hostName, gracefulShutdown)

        undertow = builder.build()
    }

    override fun startup(server: Server, settings: Map<String, *>) {
        build(server)
        if (!started)
            undertow?.start()
        started = true
    }

    override fun shutdown() {
        if (started)
            undertow?.stop()
        started = false
    }

    internal fun handleException(
        exception: Exception,
        call: Call,
        codedErrors: Map<Int, ErrorCodeCallback>,
        exceptionErrors: Map<Class<out Exception>, ExceptionCallback>,
        type: Class<*> = exception.javaClass) {

        when (exception) {
            is CodedException -> {
                val handler: ErrorCodeCallback =
                    codedErrors[exception.code] ?: { error(it, exception.message ?: "") }
                call.handler(exception.code)
            }
            else -> {
                error("Error processing request", exception)

                val handler = exceptionErrors[type]

                if (handler != null)
                    call.handler(exception)
                else
                    type.superclass.also {
                        if (it != null)
                            handleException(exception, call, codedErrors, exceptionErrors, it)
                    }
            }
        }
    }
}

