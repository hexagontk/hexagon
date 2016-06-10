package co.there4.hexagon.web

/**
 * HTTP request context. It holds client supplied data and methods to change the response.
 */
data class Exchange (
    val request: Request,
    val response: Response,
    val session: Session) {

    fun redirect (url: String) = response.redirect(url)

    fun ok(content: Any) = send (200, content)
    fun ok(code: Int = 200, content: Any = "") = send (code, content)
    fun error(code: Int = 500, content: Any = "") = send (code, content)

    fun halt(content: Any) = halt (500, content)
    fun halt(code: Int = 500, content: Any = "") {
        send (code, content)
        throw EndException ()
    }

    private fun send(code: Int, content: Any) {
        response.status = code
        response.body = content
    }
}
