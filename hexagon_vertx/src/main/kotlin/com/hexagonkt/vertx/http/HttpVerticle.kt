package com.hexagonkt.vertx.http

import com.hexagonkt.CodedException
import com.hexagonkt.Environment.cpuCount
import com.hexagonkt.Environment.hostname
import com.hexagonkt.Environment.ip
import com.hexagonkt.Environment.jvmId
import com.hexagonkt.Environment.jvmMemory
import com.hexagonkt.Environment.jvmName
import com.hexagonkt.Environment.jvmVersion
import com.hexagonkt.Environment.locale
import com.hexagonkt.Environment.timezone
import com.hexagonkt.Environment.uptime
import com.hexagonkt.Environment.usedMemory
import com.hexagonkt.logger
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import org.slf4j.Logger

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

    protected lateinit var server: HttpServer

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

        log.info("SETTINGS ${config().encodePrettily()}")

        val router = router()
        router.route().failureHandler(::failureHandler)

        server.requestHandler(router::accept).listen {
            if (it.succeeded()) {
                future.complete()
                log.info(runMessage())
            }
            else {
                future.fail(it.cause())
                log.error("Error starting $name Verticle", it.cause())
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
                log.info("$name Verticle stopped")
            }
            else {
                future.fail(it.cause())
                log.error("Error stopping $name Verticle", it.cause())
            }
        }
    }

    protected open fun failureHandler(context: RoutingContext) {
        val exception = context.failure()

        val statusCode = context.statusCode().let {
            when {
                exception is CodedException -> exception.code
                it == -1 -> 500
                else -> it
            }
        }

        val message = exception?.message ?: "Error"

        val request = context.request()
        log.error("${request.method()} ${request.path()} -> $statusCode $message", exception)

        context.response().end(statusCode, message)
    }
}
