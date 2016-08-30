package co.there4.hexagon.web

import java.net.HttpCookie

/**
 * Lists would be initialized loading all elements when they are used (set it as lazy in
 * implementations) this will have a performace penalty in favor of ease of use. The alternative
 * would be using a 'Map/List wrapper that delegates calls to abstract methods in the interface
 * (I won't do this just now).
 *
 * HTTP request context. It holds client supplied data and methods to change the response.
 */
interface Request {
    val scriptName: String                 // .path_info // "/foo" (servlet path)
    val pathInfo: String                   // .path_info // "/foo"
    val path: String                       // .path // "/foo" (servlet path + path info)
    val body: String                       // request body sent by the client
    val scheme: String                     // "http"
    val port: Int                          // 80
    val method: HttpMethod                 // "GET"
    val queryString: String                // ""
    val contentLength: Long                // length of request.body
    val contentType: String?               // media type of request.body
    val host: String                       // "example.com"
    val userAgent: String                  // user agent (used by :agent condition)
    val url: String                        // "http://example.com/example/foo"
    val ip: String                         // client IP address
    val referrer: String                   // the referrer of the client or '/'
    val secure: Boolean                    // false (would be true over ssl)
    val forwarded: Boolean                 // true (if running behind a reverse proxy)
    val xhr: Boolean                       // is this an ajax request?
    val preferredType: String              // .preferred_type(t)   // 'text/html'

    val parameters: Map<String, List<String>> // ["some_param"] // value of some_param parameter
    val headers: Map<String, List<String>>    // ["SOME_HEADER"] // value of SOME_HEADER header
    val cookies: Map<String, HttpCookie>      // hash of browser cookies
    val parts: Map<String, Part>              // hash of multipart parts

    operator fun get(name: String): String? = parameters[name]?.first()

    fun accept () = headers["Accept"]

    fun parameter(name: String) = get(name) ?: error ("'$name' parameter not found")
}
