
import io.undertow.Undertow
import io.undertow.server.RoutingHandler
import io.undertow.server.handlers.PathHandler
import io.undertow.util.Headers.CONTENT_TYPE

fun main(vararg args: String) {
    val hnd = PathHandler().addExactPath("/foo") {
        it.responseSender.send("f")
    }

    hnd.addExactPath(
        "/bar",
        RoutingHandler().get("/b/:id") {
            it.responseSender.send(it.pathParameters["id"]?.first() ?: "404")
        }
    )

    val server = Undertow.builder()
        .addHttpListener(7070, "localhost")
        .setHandler(hnd)
        .setHandler {
            it.responseHeaders.put(CONTENT_TYPE, "text/plain")
            it.responseSender.send("Hello World")
        }
        .build()
    server.start()
}
