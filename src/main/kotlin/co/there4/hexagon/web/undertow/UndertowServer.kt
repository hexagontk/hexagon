package co.there4.hexagon.web.undertow

import co.there4.hexagon.web.EndException
import co.there4.hexagon.web.Exchange
import co.there4.hexagon.web.FilterOrder.*
import co.there4.hexagon.web.Server
import io.undertow.Undertow
import io.undertow.Handlers.*
import java.net.InetAddress

class UndertowServer(
    bind: InetAddress = InetAddress.getByName("localhost"), port: Int = 4321): Server(bind, port) {

    var builder: Undertow.Builder = Undertow.builder()
    var undertow: Undertow? = null

    private var started = false
    override fun started() = started

    fun build() {
        val root = routing()

        for ((route, callbacks) in routes) {
            val routeFilters = filters.filterKeys { route.path.path.startsWith(it.path.path) }
            val beforeCallbacks = routeFilters.filter { it.key.order == BEFORE }.map { it.value }
            val afterCallbacks = routeFilters.filter { it.key.order == AFTER }.map { it.value }

            root.add(route.method.toString (), route.path.path, {
                val undertowExchange = Exchange (
                    UndertowRequest (it, route),
                    UndertowResponse (it),
                    UndertowSession (it)
                )

                try {
                    beforeCallbacks.forEach { undertowExchange.(it) () }
                    undertowExchange.(callbacks) ()
                    afterCallbacks.forEach { undertowExchange.(it) () }
                }
                catch (e: EndException) {
                    // Just aborts the handler
                }
                catch (e: Exception) {
                    handleException (e, undertowExchange)
                }
                finally {
                    it.statusCode = undertowExchange.response.status
                    it.responseSender.send (undertowExchange.response.body.toString())
                    it.responseSender.close()
                }
            })
        }

        val gracefulShutdown = gracefulShutdown (root)

        builder = builder.addHttpListener(bindPort, bindAddress.hostName, gracefulShutdown)

        undertow = builder.build()
    }

    override fun startup() {
        build ()
        if (!started)
            undertow?.start ()
        started = true
    }

    override fun shutdown() {
        if (started)
            undertow?.stop ()
        started = false
    }
}

