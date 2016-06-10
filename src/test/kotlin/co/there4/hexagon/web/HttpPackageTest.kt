package co.there4.blacksheep.web

import co.there4.hexagon.web.*
import co.there4.hexagon.web.HttpMethod.*
import co.there4.hexagon.web.FilterOrder.*
import co.there4.hexagon.web.jetty.JettyServer

import org.testng.annotations.Test
import kotlin.test.assertFailsWith

@Test class HttpPackageTest {
    fun package_routes_are_stored_in_default_server () {
        assets ("/assets")
        assert (blacksheep.assets.contains("/assets"))

        after ("/after") {}
        before ("/before") {}
        after () {}
        before () {}
        assert (blacksheep.filters.containsKey(Filter(Path ("/after"), AFTER)))
        assert (blacksheep.filters.containsKey(Filter(Path ("/before"), BEFORE)))
        assert (blacksheep.filters.containsKey(Filter(Path ("/"), AFTER)))
        assert (blacksheep.filters.containsKey(Filter(Path ("/"), BEFORE)))

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
        assert (blacksheep.routes.containsKey(Route(Path ("/get"), GET)))
        assert (blacksheep.routes.containsKey(Route(Path ("/head"), HEAD)))
        assert (blacksheep.routes.containsKey(Route(Path ("/post"), POST)))
        assert (blacksheep.routes.containsKey(Route(Path ("/put"), PUT)))
        assert (blacksheep.routes.containsKey(Route(Path ("/delete"), DELETE)))
        assert (blacksheep.routes.containsKey(Route(Path ("/trace"), TRACE)))
        assert (blacksheep.routes.containsKey(Route(Path ("/options"), OPTIONS)))
        assert (blacksheep.routes.containsKey(Route(Path ("/patch"), PATCH)))
        assert (blacksheep.routes.containsKey(Route(Path ("/"), GET)))
        assert (blacksheep.routes.containsKey(Route(Path ("/"), HEAD)))
        assert (blacksheep.routes.containsKey(Route(Path ("/"), POST)))
        assert (blacksheep.routes.containsKey(Route(Path ("/"), PUT)))
        assert (blacksheep.routes.containsKey(Route(Path ("/"), DELETE)))
        assert (blacksheep.routes.containsKey(Route(Path ("/"), TRACE)))
        assert (blacksheep.routes.containsKey(Route(Path ("/"), OPTIONS)))
        assert (blacksheep.routes.containsKey(Route(Path ("/"), PATCH)))

        error(IllegalStateException::class.java) {}
        error(IllegalArgumentException::class) {}
        assert (blacksheep.errors.containsKey(IllegalStateException::class.java))
        assert (blacksheep.errors.containsKey(IllegalArgumentException::class.java))
    }

    fun default_server_can_not_be_replaced_if_running () {
        assertFailsWith<IllegalStateException>("A default server is already started") {
            blacksheep.run ()
            blacksheep = JettyServer ()
        }
    }

    fun default_server_can_be_replaced_if_not_running () {
        blacksheep.stop ()
        blacksheep = JettyServer ()
        assert (blacksheep is JettyServer)
        blacksheep.run ()
        blacksheep.stop ()
        Thread.sleep (500)
        blacksheep = JettyServer ()
        assert (blacksheep is JettyServer)
    }
}
