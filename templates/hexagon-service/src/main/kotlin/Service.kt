package ${group}

import co.there4.hexagon.web.*
import co.there4.hexagon.web.servlet.ServletServer
import kotlinx.html.*

import java.time.LocalDateTime.now
import javax.servlet.annotation.WebListener

fun routes(srv: Router = server) {
    srv.before {
        response.addHeader("Server", "Servlet/3.1")
        response.addHeader("Transfer-Encoding", "chunked")
        response.addHeader("Date", httpDate(now()))
    }

    srv.get("/text") { ok("Hello, World!", "text/plain") }

    srv.get("/page") {
        page {
            html {
                head {
                    title { +"Greetings" }
                }
                body { +"Hello, World!" }
            }
        }
    }

    srv.get("/template") {
    }
}

@WebListener class Web : ServletServer () {
    override fun init() {
        routes(this)
    }
}

fun main(args: Array<String>) {
    routes()
    run()
}
