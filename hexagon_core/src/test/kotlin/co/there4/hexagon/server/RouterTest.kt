package co.there4.hexagon.server

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

        // TODO
        val flatRequestHandlers = router.flatRequestHandlers()
        assert(flatRequestHandlers.isNotEmpty())
    }
}
