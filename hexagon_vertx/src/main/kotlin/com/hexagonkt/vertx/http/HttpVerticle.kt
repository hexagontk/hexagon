package com.hexagonkt.vertx.http

import com.hexagonkt.helpers.CodedException
import com.hexagonkt.helpers.Environment.cpuCount
import com.hexagonkt.helpers.Environment.hostname
import com.hexagonkt.helpers.Environment.ip
import com.hexagonkt.helpers.Environment.jvmId
import com.hexagonkt.helpers.Environment.jvmMemory
import com.hexagonkt.helpers.Environment.jvmName
import com.hexagonkt.helpers.Environment.jvmVersion
import com.hexagonkt.helpers.Environment.locale
import com.hexagonkt.helpers.Environment.timezone
import com.hexagonkt.helpers.Environment.uptime
import com.hexagonkt.helpers.Environment.usedMemory
import com.hexagonkt.helpers.Logger
import com.hexagonkt.helpers.logger
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler

/**
 * Extension functions MUST be public to work on crossinline blocks (a rough guess...)
 */
@Suppress("MemberVisibilityCanPrivate")
abstract class HttpVerticle(
    private val options: HttpServerOptions = HttpServerOptions().setPort (6060)
) : AbstractVerticle() {

    private val log: Logger = logger()
    private val name: String = javaClass.simpleName

    private val bodyHandler: BodyHandler = BodyHandler.create()

    private lateinit var server: HttpServer

    protected abstract fun router(): Router

    fun actualPort() = server.actualPort()

    protected fun router(block: Router.() -> Unit): Router = Router.router(vertx).let {
        // Add filters required to read body by default (TODO Check problems if added two times)
        it.post(bodyHandler)
        it.put(bodyHandler)
        it.block()
        it
    }

    protected fun router(
        path: String, block: Router.() -> Unit): Router = Router.router(vertx).let {

        // Add filters required to read body by default (TODO Check problems if added two times)
        it.post(bodyHandler)
        it.put(bodyHandler)
        it.path(path, block)
        it
    }

    fun Router.path(path: String, block: Router.() -> Unit): Router =
        this.mountSubRouter(path, router(block))

    override fun start(future: Future<Void>) {
        this.server = vertx.createHttpServer(options)

        log.info { "SETTINGS ${config().encodePrettily()}" }

        val router = router()
        router.route().failureHandler(::failureHandler)

        server.requestHandler(router::accept).listen {
            if (it.succeeded()) {
                future.complete()
                log.info { runMessage() }
            }
            else {
                future.fail(it.cause())
                log.error({ "Error starting $name Verticle" }, it.cause())
            }
        }
    }

    private fun runMessage(): String = """
        $name Verticle RUNNING

        Process <$jvmId> Running in '$hostname' ($ip) with $cpuCount CPUs ${jvmMemory()} KB
        Java $jvmVersion [$jvmName]
        Locale $locale Timezone $timezone

        Started in ${uptime()} s using ${usedMemory()} KB
        Served at http://$hostname:${server.actualPort()}
        """.trimIndent()

    override fun stop(future: Future<Void>) {
        server.close {
            if (it.succeeded()) {
                future.complete()
                log.info { "$name Verticle stopped" }
            }
            else {
                future.fail(it.cause())
                log.error({ "Error stopping $name Verticle" }, it.cause())
            }
        }
    }

    protected open fun failureHandler(context: RoutingContext) {
        val exception = context.failure()

        val statusCode = (exception as? CodedException)?.code ?: 500
        val message = exception?.message ?: "Error"

        val request = context.request()
        log.error({ "${request.method()} ${request.path()} -> $statusCode $message" }, exception)

        context.response().end(statusCode, message)
    }
}
