package co.there4.hexagon.server

import co.there4.hexagon.server.HttpMethod.*
import org.testng.annotations.Test

/**
 * TODO .
 */
@Test class RouterTest {
    fun test() {
        val router = router {
            path {
                put {}
                get("/first") {}
            }
            path("/foo") {
                get {}
                get("/bar") {}
                path("/sub") {
                    get("/route") {}
                    post {}
                }
            }
        }

        val handlers = router.flatRequestHandlers()
        assertHandler(handlers[0], "/", PUT)
        assertHandler(handlers[1], "/first", GET)
        assertHandler(handlers[2], "/foo", GET)
        assertHandler(handlers[3], "/foo/bar", GET)
        assertHandler(handlers[4], "/foo/sub/route", GET)
        assertHandler(handlers[5], "/foo/sub", POST)
    }

    private fun assertHandler(handler: RequestHandler, path: String, vararg methods: HttpMethod) {
        val route = handler.route
        assert(route.path.path == path)
        assert(route.method.containsAll(methods.toSet()))
    }
}
