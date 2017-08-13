package co.there4.hexagon.server

import co.there4.hexagon.helpers.CodedException
import co.there4.hexagon.server.HttpMethod.*
import org.testng.annotations.Test

/**
 * TODO .
 */
@Test class RouterTest {
    @Test(expectedExceptions = arrayOf(IllegalArgumentException::class))
    fun exceptionsInErrorHandlers() {
        router {
            error(CodedException::class) {}
        }
    }

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

                after ("/bar") {}
                error(404) {}
                error(IllegalArgumentException::class) {}
                assets("/assets", "/files")
            }
        }

        val handlers = router.flatRequestHandlers()
        assertHandler(handlers[0], "/", PUT)
        assertHandler(handlers[1], "/first", GET)
        assertHandler(handlers[2], "/foo", GET)
        assertHandler(handlers[3], "/foo/bar", GET)
        assertHandler(handlers[4], "/foo/sub/route", GET)
        assertHandler(handlers[5], "/foo/sub", POST)
        assertHandler(handlers[6], "/foo/bar", *ALL.toTypedArray())
        assertHandler(handlers[7], "/foo", *ALL.toTypedArray())
        assertHandler(handlers[8], "/foo", *ALL.toTypedArray())
        assertHandler(handlers[9], "/foo/files", GET)
    }

    private fun assertHandler(handler: RequestHandler, path: String, vararg methods: HttpMethod) {
        val route = handler.route
        assert(route.path.path == path)
        assert(route.methods.containsAll(methods.toSet()))
    }
}
