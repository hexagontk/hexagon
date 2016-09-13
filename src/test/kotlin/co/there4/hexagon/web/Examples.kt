package co.there4.hexagon.web

import co.there4.hexagon.web.jetty.JettyServer
import java.net.InetAddress.getByName as address

val SESSION_NAME = "username"

val usernamePasswords = mapOf (
    Pair ("foo", "bar"),
    Pair ("admin", "admin")
)

fun filterExample(context: String = "filter") {
    before {
        val user = request.parameters ["user"] ?: ""
        val password = request.parameters ["password"] ?: ""

        val dbPassword = usernamePasswords[user]
        if (password != dbPassword)
            halt (401, "You are not welcome here!!!")
    }

    before ("/$context/hello") { response.addHeader ("Foo", "Set by second before filter") }

    get ("/$context/hello") { ok ("Hello World!") }

    after ("/$context/hello") { response.addHeader ("hexagon", "added by after-filter") }
}

fun helloWorld(context: String = "hello") {
    get ("/$context") { ok ("Hello World!") }
}

fun simpleExample(context: String = "simple") {
    get ("/$context/hello") { ok ("Hello World!") }

    post ("/$context/hello") { ok ("Hello World: " + response.body) }

    get ("/$context/private") {
        response.status = 401
        ok ("Go Away!!!")
    }

    get ("/$context/users/{name}") { ok ("Selected user: " + request.parameters ["name"]) }

    get ("/$context/news/{section}") {
        response.addHeader ("Content-Type", "text/xml")
        ok (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<news>" + request.parameters ["section"] + "</news>"
        )
    }

    get ("/$context/protected") {
        halt (403, "I don't think so!!!")
    }

    get ("/$context/redirect") {
        redirect ("/news/world")
    }

    get ("/$context/") { ok ("root") }
}

fun sessionExample(context: String = "session") {
    get ("/$context/") {
        ok (
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
        )
    }

    post ("/$context/entry") {
        val name = request.parameters ["name"]?.first()

        if (name != null)
            session[SESSION_NAME] = name

        redirect ("/")
    }

    get ("/$context/clear") {
        session.remove (SESSION_NAME)
        redirect ("/")
    }
}

fun main(args: Array<String>) {
    server = JettyServer (bindAddress = address ("localhost"), bindPort = 8080)

    filterExample ()
    helloWorld()
    simpleExample()
    sessionExample()

    server.run ()
}
