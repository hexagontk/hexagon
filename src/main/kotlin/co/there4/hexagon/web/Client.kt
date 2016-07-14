package co.there4.hexagon.web

import co.there4.hexagon.serialization.defaultFormat
import co.there4.hexagon.serialization.serialize
import java.net.URL

import org.asynchttpclient.request.body.multipart.Part as AsyncHttpPart
import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.Response
import org.asynchttpclient.cookie.Cookie

import co.there4.hexagon.web.HttpMethod.*
import org.asynchttpclient.DefaultAsyncHttpClientConfig

/**
 * Client to use other REST services (like the ones created with Blacksheep).
 */
class Client (val endpointUrl: URL, val useCookies: Boolean = true) {
    val endpoint = endpointUrl.toString()
    val client = DefaultAsyncHttpClient(DefaultAsyncHttpClientConfig.Builder()
        .setConnectTimeout(5000)
        .build())
    val cookies: MutableMap<String, Cookie> = mutableMapOf()

    /**
     * Synchronous execution.
     */
    fun send (
        method: HttpMethod,
        url: String = "/",
        body: String? = null,
        contentType: String? = null): Response {

        val request = createRequest(method, url, contentType)

        if (body != null)
            request.setBody(body)

        if (useCookies)
            cookies.forEach { request.addCookie(it.value) }

        val response = request.execute().get()

        if (useCookies) {
            response.cookies.forEach {
                if (it.value == "")
                    cookies.remove(it.name)
                else
                    cookies[it.name] = it
            }
        }

        return response
    }

    fun sendData(method: HttpMethod, url: String, contentType: String, body: Any) =
        send(method, url, body.serialize(contentType), contentType)

    fun get (url: String, body: String? = null) = send (GET, url, body)
    fun head (url: String, body: String? = null) = send (HEAD, url, body)
    fun post (url: String, body: String? = null) = send (POST, url, body)
    fun put (url: String, body: String? = null) = send (PUT, url, body)
    fun delete (url: String, body: String? = null) = send (DELETE, url, body)
    fun trace (url: String, body: String? = null) = send (TRACE, url, body)
    fun options (url: String, body: String? = null) = send (OPTIONS, url, body)
    fun patch (url: String, body: String? = null) = send (PATCH, url, body)

    fun get (url: String, contentType: String, body: Any) =
        sendData (GET, url, contentType, body)

    fun head (url: String, contentType: String, body: Any) =
        sendData (HEAD, url, contentType, body)

    fun post (url: String, contentType: String, body: Any) =
        sendData (POST, url, contentType, body)

    fun put (url: String, contentType: String, body: Any) =
        sendData (PUT, url, contentType, body)

    fun delete (url: String, contentType: String, body: Any) =
        sendData (DELETE, url, contentType, body)

    fun trace (url: String, contentType: String, body: Any) =
        sendData (TRACE, url, contentType, body)

    fun options (url: String, contentType: String, body: Any) =
        sendData (OPTIONS, url, contentType, body)

    fun patch (url: String, contentType: String, body: Any) =
        sendData (PATCH, url, contentType, body)

    private fun createRequest(method: HttpMethod, url: String, contentType: String? = null) =
        (endpoint + url).let {
            val request = when (method) {
                GET -> client.prepareGet (it)
                HEAD -> client.prepareHead (it)
                POST -> client.preparePost (it)
                PUT -> client.preparePut (it)
                DELETE -> client.prepareDelete (it)
                TRACE -> client.prepareTrace (it)
                OPTIONS -> client.prepareOptions (it)
                PATCH -> client.preparePatch (it)
            }

            if (contentType != null) request.addHeader("Content-Type", contentType)
            else request
        }
}
