package com.hexagonkt.http.server

import com.hexagonkt.http.Method
import com.hexagonkt.http.Method.*
import com.hexagonkt.helpers.CodedException
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

    private fun assertHandler(handler: RequestHandler, path: String, vararg methods: Method) {
        val route = handler.route
        assert(route.path.path == path)
        assert(route.methods.containsAll(methods.toSet()))
    }
}
