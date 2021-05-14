package com.hexagonkt.http.server

import com.hexagonkt.http.ALL
import com.hexagonkt.http.Method
import com.hexagonkt.http.Method.DELETE
import com.hexagonkt.http.Method.GET
import com.hexagonkt.http.Method.HEAD
import com.hexagonkt.http.Method.OPTIONS
import com.hexagonkt.http.Method.PATCH
import com.hexagonkt.http.Method.POST
import com.hexagonkt.http.Method.PUT
import com.hexagonkt.http.Method.TRACE
import com.hexagonkt.http.Path
import com.hexagonkt.http.Route
import com.hexagonkt.http.server.FilterOrder.AFTER
import com.hexagonkt.http.server.FilterOrder.BEFORE
import com.hexagonkt.http.server.RequestHandler.CodeHandler
import com.hexagonkt.http.server.RequestHandler.ExceptionHandler
import com.hexagonkt.http.server.RequestHandler.FilterHandler
import com.hexagonkt.http.server.RequestHandler.PathHandler
import com.hexagonkt.http.server.RequestHandler.ResourceHandler
import com.hexagonkt.http.server.RequestHandler.RouteHandler
import org.junit.jupiter.api.Test
import java.net.URL

internal class RouterTest {

    @Test fun `Nested routes are flattened properly inside by Router`() {
        val router = Router {
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

                after("/bar") {}
                error(404) {}
                error(IllegalArgumentException::class) {}
                get("/files", URL("classpath:/assets"))
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

    @Test fun `Routes are stored in server's router`() {
        val server = Server(VoidAdapter) {
            get(URL("classpath:assets"))

            after("/after") {}
            before("/before") {}
            after {}
            before {}

            before("/infix") { response.headers["infix"] = "before" }

            get("/get") {}
            head("/head") {}
            post("/post") {}
            put("/put") {}
            delete("/delete") {}
            trace("/trace") {}
            options("/options") {}
            patch("/patch") {}
            get { ok("get") }
            head { ok("head") }
            post { ok("post") }
            put { ok("put") }
            delete { ok("delete") }
            trace { ok("trace") }
            options { ok("options") }
            patch { ok("patch") }

            get("/infix") { ok("infix") }

            path("/router") {
                get("/subRoute") { ok("Router") }
            }

            error(401) {}
            error(IllegalStateException::class.java) {}
            error(IllegalArgumentException::class) {}
        }

        val requestHandlers = server.contextRouter.requestHandlers

        val assets = requestHandlers.filterIsInstance(ResourceHandler::class.java)
        assert (assets.any { it.route.path.pattern == "/*" && it.resource.path == "assets" })

        val filters = requestHandlers.filterIsInstance(FilterHandler::class.java)
        assert(filters.any { it.route == Route(Path("/after"), ALL) && it.order == AFTER })
        assert(filters.any { it.route == Route(Path("/before"), ALL) && it.order == BEFORE })
        assert(filters.any { it.route == Route(Path("*"), ALL) && it.order == AFTER })
        assert(filters.any { it.route == Route(Path("*"), ALL) && it.order == BEFORE })
        assert(filters.any { it.route == Route(Path("/infix"), ALL) && it.order == BEFORE })

        val routes = requestHandlers.filterIsInstance(RouteHandler::class.java)
        assert(routes.any { it.route == Route(Path("/get"), GET) })
        assert(routes.any { it.route == Route(Path("/head"), HEAD) })
        assert(routes.any { it.route == Route(Path("/post"), POST) })
        assert(routes.any { it.route == Route(Path("/put"), PUT) })
        assert(routes.any { it.route == Route(Path("/delete"), DELETE) })
        assert(routes.any { it.route == Route(Path("/trace"), TRACE) })
        assert(routes.any { it.route == Route(Path("/options"), OPTIONS) })
        assert(routes.any { it.route == Route(Path("/patch"), PATCH) })
        assert(routes.any { it.route == Route(Path("/"), GET) })
        assert(routes.any { it.route == Route(Path("/"), HEAD) })
        assert(routes.any { it.route == Route(Path("/"), POST) })
        assert(routes.any { it.route == Route(Path("/"), PUT) })
        assert(routes.any { it.route == Route(Path("/"), DELETE) })
        assert(routes.any { it.route == Route(Path("/"), TRACE) })
        assert(routes.any { it.route == Route(Path("/"), OPTIONS) })
        assert(routes.any { it.route == Route(Path("/"), PATCH) })
        assert(routes.any { it.route == Route(Path("/infix"), GET) })

        val paths = requestHandlers.filterIsInstance(PathHandler::class.java)
        val subRouter = paths.first { it.route == Route(Path("/router")) }.router
        val subGet = subRouter.requestHandlers.filterIsInstance(RouteHandler::class.java).first()
        assert(subGet.route.path.pattern == "/subRoute")

        val codedErrors = requestHandlers.filterIsInstance(CodeHandler::class.java)
        assert (codedErrors.any { it.code == 401 })
        val exceptionErrors = requestHandlers.filterIsInstance(ExceptionHandler::class.java)
        assert(exceptionErrors.any { it.exception == IllegalArgumentException::class.java })
        assert(exceptionErrors.any { it.exception == IllegalArgumentException::class.java })
    }

    private fun assertHandler(handler: RequestHandler, path: String, vararg methods: Method) {
        val route = handler.route
        assert(route.path.pattern == path)
        assert(route.methods.containsAll(methods.toSet()))
    }
}
