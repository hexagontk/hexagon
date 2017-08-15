package com.hexagonkt.server

import java.net.InetAddress.getByName as address
import com.hexagonkt.server.HttpMethod.*
import java.net.HttpCookie

/** A route, available in the server (to be handled) or in the client (to be * called). */
val getIndex = get()
/** Another syntax to create a route. */
val postIndex = POST at "/"

const val SESSION_NAME = "username"

val usernamePasswords = mapOf (
    "foo" to "bar",
    "admin" to "admin"
)

/**
 * Creates a server which can be started/stoped elsewhere.
 *
 * Each call defines a route handler or filter, they are evaluated in order at runtime.
 */
val serverExample = server(VoidEngine) {
    // Adds 'foo' header to all requests
    before { response.addHeader("foo", "bar") }

    // Before POST / check 'pass' header, if present, pass to the next filter
    postIndex before {
        if(request.headers["odd"] != null)
            response.addHeader("odd", "true")
        else
            response.addHeader("even", "true")
    }

    // Another syntax for a before filter
    ALL at "/" before { response.addCookie(HttpCookie("cookie", "jar")) }

    // Map '/public' classpath resources to '/' path
    assets ("/public")
    // Map '/css' classpath resources to '/css' path (only if not found in '/public' before!)
    assets ("/css", "/public")

    get { "Hi" }

    // Create a handler with an existing route
    getIndex by {}

    post("/foo") by {}

    // The return object is the response (String sets body, Int sets code, Pair<Int, Any> both)
    POST at "/foo" by { "Done" }
    // Handling a route with a method reference (note that 'Call' cannot be used as the receiver)
    POST at "/foo" by ::reference

    ALL at "/" after {}
}

private fun reference(@Suppress("UNUSED_PARAMETER") e: Call): Any = 200 to "Done"

val filter = router {
    before {
        val user = request.parameters ["user"] ?: ""
        val password = request.parameters ["password"] ?: ""

        val dbPassword = usernamePasswords[user]
        if (password != dbPassword)
            401 to "You are not welcome here!!!"
    }

    before ("/hello") { response.addHeader ("Foo", "Set by second before filter") }
    get ("/hello") { "Hello World!" }
    after ("/hello") { response.addHeader ("hexagon", "added by after-filter") }
}

val helloWorld = router {
    get ("/") { "Hello World!" }
}

val simple = router {
    get ("/news/{section}") {
        val list = request.parameters ["section"]?.first() ?: "not found"
        ok("""<?xml version="1.0" encoding="UTF-8"?><news>$list</news>""", "text/xml")
    }

    get ("/hello") { "Hello World!" }
    post ("/hello") { "Hello World: " + response.body }
    // Returning a tuple sets the code and the body
    get ("/private") { 401 to "Go Away!!!" }
    get ("/users/{name}") { "Selected user: " + request.parameters ["name"] }
    get ("/protected") { 403 to "I don't think so!!!" }
    get ("/redirect") { redirect ("/news/world") }
    get { "root" }
}

val session = router {
    get {
        if (session.attributes.containsKey(SESSION_NAME))
            """
            <html>
                <body>
                    What's your name?:
                    <form action="/entry" method="POST">
                        <input type="text" name="name"/>
                        <input type="submit" value="go"/>
                    </form>
                </body>
            </html>
            """
        else
            "<html><body>Hello, ${session[SESSION_NAME]}!</body></html>"
    }

    post {
        val name = request.parameters ["name"]?.first()

        if (name != null)
            session[SESSION_NAME] = name

        redirect ("/")
    }

    get {
        session.removeAttribute (SESSION_NAME)
        redirect ("/")
    }
}

fun main(args: Array<String>) {
    serve(VoidEngine) {
        // You can mount routers in paths
        path("/filter", filter)
        path("/hello", helloWorld)
        path("/simple", simple)
        path("/session", session)

        // Handlers can be nested
        path("/inline") {
            get { "Inline router" }
            post {}
        }
    }

    serverExample.run()
}
