package com.hexagonkt.server

import com.hexagonkt.http.Method.*
import com.hexagonkt.server.FilterOrder.*
import com.hexagonkt.server.RequestHandler.*
import com.hexagonkt.http.Path
import com.hexagonkt.http.Route

import org.testng.annotations.Test

@Test class ServerPackageTest {

    fun package_routes_are_stored_in_server () {
        val server = Server(VoidAdapter) {
            assets ("assets")

            after ("/after") {}
            before ("/before") {}
            after {}
            before {}

            before("/infix") { response.addHeader("infix", "before") }

            get ("/get") {}
            head ("/head") {}
            post ("/post") {}
            put ("/put") {}
            delete ("/delete") {}
            trace ("/trace") {}
            options ("/options") {}
            patch ("/patch") {}
            get { "get" }
            head { "head" }
            post { "post" }
            put { "put" }
            delete { "delete" }
            trace { "trace" }
            options { "options" }
            patch { "patch" }

            get("/infix") by { "infix" }

            path("/router") mount router {
                get("/subroute") { "Router" }
            }

            error(401) {}
            error(IllegalStateException::class.java) {}
            error(IllegalArgumentException::class) {}
        }

        val assets = server.router.requestHandlers.filterIsInstance(AssetsHandler::class.java)
        assert (assets.any { it.route.path.path == "/*" && it.path == "assets" })

        val filters = server.router.requestHandlers.filterIsInstance(FilterHandler::class.java)
        assert (filters.any { it.route == Route(Path("/after"), ALL) && it.order == AFTER })
        assert (filters.any { it.route == Route(Path("/before"), ALL) && it.order == BEFORE })
        assert (filters.any { it.route == Route(Path("/*"), ALL) && it.order == AFTER })
        assert (filters.any { it.route == Route(Path("/*"), ALL) && it.order == BEFORE })
        assert (filters.any { it.route == Route(Path("/infix"), ALL) && it.order == BEFORE })

        val routes = server.router.requestHandlers.filterIsInstance(RouteHandler::class.java)
        assert (routes.any { it.route == Route(Path("/get"), GET) })
        assert (routes.any { it.route == Route(Path("/head"), HEAD) })
        assert (routes.any { it.route == Route(Path("/post"), POST) })
        assert (routes.any { it.route == Route(Path("/put"), PUT) })
        assert (routes.any { it.route == Route(Path("/delete"), DELETE) })
        assert (routes.any { it.route == Route(Path("/trace"), TRACE) })
        assert (routes.any { it.route == Route(Path("/options"), OPTIONS) })
        assert (routes.any { it.route == Route(Path("/patch"), PATCH) })
        assert (routes.any { it.route == Route(Path("/"), GET) })
        assert (routes.any { it.route == Route(Path("/"), HEAD) })
        assert (routes.any { it.route == Route(Path("/"), POST) })
        assert (routes.any { it.route == Route(Path("/"), PUT) })
        assert (routes.any { it.route == Route(Path("/"), DELETE) })
        assert (routes.any { it.route == Route(Path("/"), TRACE) })
        assert (routes.any { it.route == Route(Path("/"), OPTIONS) })
        assert (routes.any { it.route == Route(Path("/"), PATCH) })
        assert (routes.any { it.route == Route(Path("/infix"), GET) })

        val paths = server.router.requestHandlers.filterIsInstance(PathHandler::class.java)
        val subrouter = paths.first { it.route == Route(Path("/router")) }.router
        val subget = subrouter.requestHandlers.filterIsInstance(RouteHandler::class.java).first()
        assert(subget.route.path.path == "/subroute")

        val codedErrors = server.router.requestHandlers.filterIsInstance(CodeHandler::class.java)
        assert (codedErrors.any { it.code == 401 })
        val exceptionErrors = server.router.requestHandlers.filterIsInstance(ExceptionHandler::class.java)
        assert (exceptionErrors.any { it.exception == IllegalArgumentException::class.java })
        assert (exceptionErrors.any { it.exception == IllegalArgumentException::class.java })
    }
}
