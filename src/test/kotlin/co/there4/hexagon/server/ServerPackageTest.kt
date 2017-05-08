package co.there4.hexagon.server

import co.there4.hexagon.server.HttpMethod.*
import co.there4.hexagon.server.FilterOrder.*
import co.there4.hexagon.server.RequestHandler.*

import org.testng.annotations.Test

@Test class ServerPackageTest {
    fun package_routes_are_stored_in_default_server () {
        val server = server {
            assets ("/assets")
            val assets = requestHandlers.filterIsInstance(AssetsHandler::class.java)
            assert (assets.any { it.route.path.path == "/" && it.path == "/assets" })

            after ("/after") {}
            before ("/before") {}
            after {}
            before {}
            val filters = requestHandlers.filterIsInstance(FilterHandler::class.java)
            assert (filters.any { it.route == Route(Path ("/after"), ALL) && it.order == AFTER })
            assert (filters.any { it.route == Route(Path ("/before"), ALL) && it.order == BEFORE })
            assert (filters.any { it.route == Route(Path ("/*"), ALL) && it.order == AFTER })
            assert (filters.any { it.route == Route(Path ("/*"), ALL) && it.order == BEFORE })

            get ("/get") {}
            head ("/head") {}
            post ("/post") {}
            put ("/put") {}
            delete ("/delete") {}
            trace ("/trace") {}
            options ("/options") {}
            patch ("/patch") {}
            get {}
            head {}
            post {}
            put {}
            delete {}
            trace {}
            options {}
            patch {}

            path("/router") mount router {
                get { "Router" }
            }

            error(IllegalStateException::class.java) {}
            error(IllegalArgumentException::class) {}
        }

        val routes = server.router.requestHandlers.filterIsInstance(RouteHandler::class.java)
        assert (routes.any { it.route == Route(Path ("/get"), GET) })
        assert (routes.any { it.route == Route(Path ("/head"), HEAD) })
        assert (routes.any { it.route == Route(Path ("/post"), POST) })
        assert (routes.any { it.route == Route(Path ("/put"), PUT) })
        assert (routes.any { it.route == Route(Path ("/delete"), DELETE) })
        assert (routes.any { it.route == Route(Path ("/trace"), TRACE) })
        assert (routes.any { it.route == Route(Path ("/options"), OPTIONS) })
        assert (routes.any { it.route == Route(Path ("/patch"), PATCH) })
        assert (routes.any { it.route == Route(Path ("/"), GET) })
        assert (routes.any { it.route == Route(Path ("/"), HEAD) })
        assert (routes.any { it.route == Route(Path ("/"), POST) })
        assert (routes.any { it.route == Route(Path ("/"), PUT) })
        assert (routes.any { it.route == Route(Path ("/"), DELETE) })
        assert (routes.any { it.route == Route(Path ("/"), TRACE) })
        assert (routes.any { it.route == Route(Path ("/"), OPTIONS) })
        assert (routes.any { it.route == Route(Path ("/"), PATCH) })

        val paths = server.router.requestHandlers.filterIsInstance(PathHandler::class.java)
        assert (paths.any { it.route == Route(Path ("/router")) })

        assert (server.router.exceptionErrors.containsKey(IllegalStateException::class.java))
        assert (server.router.exceptionErrors.containsKey(IllegalArgumentException::class.java))

        server.router.reset()
        assert(server.router.requestHandlers.size == 2)
    }
}
