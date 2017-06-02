//package co.there4.hexagon.server.engine.undertow
//
//import co.there4.hexagon.server.Call
//import co.there4.hexagon.server.FilterOrder.*
//import co.there4.hexagon.server.Server
//import co.there4.hexagon.server.ServerEngine
//import io.undertow.Undertow
//import io.undertow.Handlers.*
//import io.undertow.Undertow.Builder
//import java.net.InetAddress
//import java.net.InetAddress.getByName as address
//
//class UndertowServer(bindAddress: InetAddress = address("localhost"), bindPort: Int = 2010):
//    ServerEngine(bindAddress, bindPort) {
//
//    var builder: Builder = Undertow.builder()
//    var undertow: Undertow? = null
//
//    private var started = false
//    override fun started() = started
//
//    fun build() {
//        val root = routing()
//
//        for ((route, callbacks) in routes) {
//            val routeFilters = filters.filterKeys { route.path.path.startsWith(it.path.path) }
//            val beforeCallbacks = routeFilters.filter { it.key.order == BEFORE }.map { it.value }
//            val afterCallbacks = routeFilters.filter { it.key.order == AFTER }.map { it.value }
//
//            root.add(route.method.toString (), route.path.path, {
//                val undertowExchange = Call (
//                    UndertowRequest (it, route),
//                    UndertowResponse (it),
//                    UndertowSession (it)
//                )
//
//                try {
//                    beforeCallbacks.forEach { undertowExchange.(it) () }
//                    undertowExchange.(callbacks) ()
//                    afterCallbacks.forEach { undertowExchange.(it) () }
//                }
//                catch (e: EndException) {
//                    // Just aborts the handler
//                }
//                catch (e: Exception) {
//                    handleException (e, undertowExchange)
//                }
//                finally {
//                    it.statusCode = undertowExchange.response.status
//                    it.responseSender.send (undertowExchange.response.body.toString())
//                    it.responseSender.close()
//                }
//            })
//        }
//
//        val gracefulShutdown = gracefulShutdown (root)
//
//        builder = builder.addHttpListener(bindPort, bindAddress.hostName, gracefulShutdown)
//
//        undertow = builder.build()
//    }
//
//    override fun startup(server: Server, settings: Map<String, *>) {
//        build ()
//        if (!started)
//            undertow?.start ()
//        started = true
//    }
//
//    override fun shutdown() {
//        if (started)
//            undertow?.stop ()
//        started = false
//    }
//}
//
