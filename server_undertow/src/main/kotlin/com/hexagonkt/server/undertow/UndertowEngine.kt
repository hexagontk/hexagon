package com.hexagonkt.server.undertow

import java.lang.ClassLoader.getSystemClassLoader

import com.hexagonkt.helpers.error
import com.hexagonkt.helpers.CachedLogger
import com.hexagonkt.helpers.CodedException
import com.hexagonkt.serialization.serialize
import com.hexagonkt.server.*
import com.hexagonkt.server.FilterOrder.AFTER
import com.hexagonkt.server.FilterOrder.BEFORE
import com.hexagonkt.server.RequestHandler.*
import io.undertow.Undertow
import io.undertow.Handlers.*
import io.undertow.predicate.Predicates
import io.undertow.server.HttpHandler
import io.undertow.server.handlers.BlockingHandler
import io.undertow.server.handlers.resource.ResourceHandler
import io.undertow.server.session.InMemorySessionManager
import io.undertow.server.session.SessionAttachmentHandler
import io.undertow.server.session.SessionCookieConfig
import io.undertow.util.AttachmentKey
import java.net.InetSocketAddress
import java.net.InetAddress.getByName as address
import io.undertow.server.handlers.PredicateHandler
import io.undertow.server.handlers.resource.ClassPathResourceManager

class UndertowEngine : ServerPort {
    companion object : CachedLogger(UndertowEngine::class)

    private val callKey = AttachmentKey.create(Call::class.java)

    private val notFoundHandler: ErrorCodeCallback = { error(404, "${request.url} not found") }
    private val baseExceptionHandler: ExceptionCallback =
        { error(500, "${it.javaClass.simpleName} (${it.message ?: "no details"})") }

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

        val errorHandlers = listOf(
            CodeHandler(Route(Path("/"), ALL), 404, notFoundHandler),
            ExceptionHandler(Route(Path("/"), ALL), Exception::class.java, baseExceptionHandler)
        )

        val requestHandlers = errorHandlers + server.router.flatRequestHandlers()
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
                is RouteHandler -> {
                    route.methods.forEach { m ->
                        root.add(m.toString (), route.path.path, BlockingHandler {
                            val call = it.getAttachment(callKey)
                            val handlerCallback = handler.callback
                            val result = call.handlerCallback()
                            val response = call.response

                            // TODO warn if body has been set
                            when (result) {
                                is Unit -> {
                                    if (!response.statusChanged && response.status != 302)
                                        response.status = 200
                                }
                                is Nothing -> {
                                    if (!response.statusChanged && response.status != 302)
                                        response.status = 200
                                }
                                is Int -> response.status = result
                                is String -> call.ok(result)
                                is Pair<*, *> -> call.ok(
                                    code = result.first as? Int ?: 200,
                                    content = result.second.let {
                                        it as? String
                                            ?: it?.serialize(call.serializationFormat())
                                            ?: ""
                                    }
                                )
                                else -> call.ok(result.serialize(call.serializationFormat()))
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

        // TODO Code a proper handler
        val hasAssets = requestHandlers.filterIsInstance<AssetsHandler>().isNotEmpty()
        val resourceHandler =
            if (hasAssets)
                PredicateHandler(
                    Predicates.suffixes(".css", ".js", ".txt"),
                    ResourceHandler(ClassPathResourceManager (getSystemClassLoader (), "public")),
                    sessionHandler
                )
            else
                sessionHandler

        val bindPort = server.bindPort
        val hostName = server.bindAddress.hostName
        val gracefulShutdown = gracefulShutdown(resourceHandler)
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

    private fun handleException(
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
