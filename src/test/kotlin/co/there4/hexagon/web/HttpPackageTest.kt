package co.there4.hexagon.web

import co.there4.hexagon.web.HttpMethod.*
import co.there4.hexagon.web.FilterOrder.*

import org.testng.annotations.Test

@Test class HttpPackageTest {
    fun package_routes_are_stored_in_default_server () {
        val server = server {
            assets ("/assets")
            assert (assets.contains("/assets"))

            after ("/after") {}
            before ("/before") {}
            after {}
            before {}
            assert (filters.containsKey(Filter(Path ("/after"), AFTER)))
            assert (filters.containsKey(Filter(Path ("/before"), BEFORE)))
            assert (filters.containsKey(Filter(Path ("/*"), AFTER)))
            assert (filters.containsKey(Filter(Path ("/*"), BEFORE)))

            get ("/get") {}
            head ("/head") {}
            post ("/post") {}
            put ("/put") {}
            delete ("/delete") {}
            trace ("/trace") {}
            options ("/options") {}
            patch ("/patch") {}
            get ("/") {}
            head ("/") {}
            post ("/") {}
            put ("/") {}
            delete ("/") {}
            trace ("/") {}
            options ("/") {}
            patch ("/") {}

            error(IllegalStateException::class.java) {}
            error(IllegalArgumentException::class) {}
        }

        assert (server.routes.containsKey(Route(Path ("/get"), GET)))
        assert (server.routes.containsKey(Route(Path ("/head"), HEAD)))
        assert (server.routes.containsKey(Route(Path ("/post"), POST)))
        assert (server.routes.containsKey(Route(Path ("/put"), PUT)))
        assert (server.routes.containsKey(Route(Path ("/delete"), DELETE)))
        assert (server.routes.containsKey(Route(Path ("/trace"), TRACE)))
        assert (server.routes.containsKey(Route(Path ("/options"), OPTIONS)))
        assert (server.routes.containsKey(Route(Path ("/patch"), PATCH)))
        assert (server.routes.containsKey(Route(Path ("/"), GET)))
        assert (server.routes.containsKey(Route(Path ("/"), HEAD)))
        assert (server.routes.containsKey(Route(Path ("/"), POST)))
        assert (server.routes.containsKey(Route(Path ("/"), PUT)))
        assert (server.routes.containsKey(Route(Path ("/"), DELETE)))
        assert (server.routes.containsKey(Route(Path ("/"), TRACE)))
        assert (server.routes.containsKey(Route(Path ("/"), OPTIONS)))
        assert (server.routes.containsKey(Route(Path ("/"), PATCH)))

        assert (server.errors.containsKey(IllegalStateException::class.java))
        assert (server.errors.containsKey(IllegalArgumentException::class.java))
    }
}
