package co.there4.hexagon.server.undertow

import co.there4.hexagon.helpers.error
import co.there4.hexagon.helpers.CachedLogger
import co.there4.hexagon.helpers.CodedException
import co.there4.hexagon.serialization.serialize
import co.there4.hexagon.server.*
import co.there4.hexagon.server.FilterOrder.AFTER
import co.there4.hexagon.server.FilterOrder.BEFORE
import co.there4.hexagon.server.RequestHandler.*
import io.undertow.Undertow
import io.undertow.Handlers.*
import io.undertow.server.HttpHandler
import io.undertow.server.handlers.BlockingHandler
import io.undertow.server.session.InMemorySessionManager
import io.undertow.server.session.SessionAttachmentHandler
import io.undertow.server.session.SessionCookieConfig
import io.undertow.util.AttachmentKey
import java.net.InetSocketAddress
import java.net.InetAddress.getByName as address

class UndertowEngine : ServerEngine {
    companion object : CachedLogger(UndertowEngine::class)

    private val callKey = AttachmentKey.create(Call::class.java)

    private var undertow: Undertow? = null
    private var started = false

    override fun started() = started

    override fun runtimePort(): Int =
        (undertow?.listenerInfo?.get(0)?.address as? InetSocketAddress)?.port ?: error

    fun build(server: Server) {
        val root = routing()
        val before = routing()
        val after = routing()
        before.invalidMethodHandler = HttpHandler {}
        before.fallbackHandler = HttpHandler {}
        after.invalidMethodHandler = HttpHandler {}
        after.fallbackHandler = HttpHandler {}

        val requestHandlers = server.router.flatRequestHandlers()
        val filtersByOrder = requestHandlers
            .filterIsInstance(FilterHandler::class.java)
            .groupBy { it.order }

        val beforeFilters = filtersByOrder[BEFORE] ?: listOf()
        val afterFilters = filtersByOrder[AFTER] ?: listOf()

        val codedErrors: Map<Int, ErrorCodeCallback> = requestHandlers
            .filterIsInstance(CodeHandler::class.java)
            .map { it.code to it.callback }
            .toMap()

        val exceptionErrors: Map<Class<out Exception>, ExceptionCallback> = requestHandlers
            .filterIsInstance(ExceptionHandler::class.java)
            .map { it.exception to it.callback }
            .toMap()

        beforeFilters.forEach { (filterRoute, _, handlerCallback) ->
            filterRoute.methods.forEach { method ->
                before.add(method.toString(), filterRoute.path.path, BlockingHandler {
                    val call = it.getAttachment(callKey)
                    call.handlerCallback()
                })
            }
        }

        for (handler in requestHandlers) {
            val route = handler.route

            when (handler) {
                is AssetsHandler -> {}
                is RouteHandler -> {
                    route.methods.forEach { m ->
                        root.add(m.toString (), route.path.path, BlockingHandler {
                            val call = it.getAttachment(callKey)
                            val handlerCallback = handler.callback
                            val result = call.handlerCallback()

                            // TODO warn if body has been set
                            when (result) {
                                is Unit -> { if (!call.response.statusChanged) call.response.status = 200 }
                                is Nothing -> { if (!call.response.statusChanged) call.response.status = 200 }
                                is Int -> call.response.status = result
                                is String -> call.ok(result)
                                is Pair<*, *> -> call.ok(
                                    code = result.first as? Int ?: 200,
                                    content = result.second.let {
                                        it as? String ?: it?.serialize(call.contentType()) ?: ""
                                    }
                                )
                                else -> call.ok(result.serialize(call.contentType()))
                            }
                        })
                    }
                }
                else -> warn("unhandled")
            }
        }

        afterFilters.forEach { (filterRoute, _, handlerCallback) ->
            filterRoute.methods.forEach { method ->
                after.add(method.toString(), filterRoute.path.path, BlockingHandler {
                    val call = it.getAttachment(callKey)
                    call.handlerCallback()
                })
            }
        }

        val sessionHandler = SessionAttachmentHandler(
            InMemorySessionManager("session_manager"),
            SessionCookieConfig())

        sessionHandler.next = BlockingHandler {
            val call = Call (
                Request(UndertowRequest (it)),
                Response(UndertowResponse (it)),
                Session(UndertowSession (it))
            )

            it.putAttachment(callKey, call)

            try {
                before.handleRequest(it)
                root.handleRequest(it)
                after.handleRequest(it)
            }
            catch (e: Exception) {
                handleException (e, call, codedErrors, exceptionErrors)
            }
            finally {
                it.responseSender.send (call.response.body.toString())
                it.responseSender.close()
            }
        }

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

