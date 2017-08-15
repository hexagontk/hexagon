package ${group}

import java.time.LocalDateTime.now
import javax.servlet.annotation.WebListener

import kotlinx.html.*
import com.hexagonkt.web.*
import com.hexagonkt.web.servlet.ServletServer
import com.hexagonkt.template.KotlinxHtmlRenderer.page

/**
 * Routes setup. It is in its own method to be able to call it for the Webapp and from the Service.
 *
 * @receiver The router to be configured.
 */
fun Router.routes() {
    before {
        response.addHeader("Server", "Servlet/3.1")
        response.addHeader("Transfer-Encoding", "chunked")
        response.addHeader("Date", httpDate(now()))
    }

    get("/text") { ok("Hello, World!", "text/plain") }

    get("/page") {
        page {
            html {
                head {
                    title { +"Greetings" }
                }
                body { +"Hello, World!" }
            }
        }
    }

    get("/template") { template("page.html") }
}

/**
 * Main Webapp class.
 */
@WebListener class Web : ServletServer () {
    override fun init() {
        this.routes()
    }
}

/**
 * Start the service from the command line.
 */
fun main(args: Array<String>) {
    server.routes()
    run()
}
