package com.hexagonkt.http.server

import com.hexagonkt.helpers.logger
import com.hexagonkt.http.Method
import com.hexagonkt.http.Part
import com.hexagonkt.serialization.SerializationFormat
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.SerializationManager.defaultFormat
import com.hexagonkt.serialization.convertToObject
import com.hexagonkt.serialization.parse
import com.hexagonkt.serialization.parseObjects
import java.net.HttpCookie
import java.security.cert.X509Certificate
import kotlin.reflect.KClass

/**
 * Lists would be initialized loading all elements when they are used (set it as lazy in
 * implementations) this will have a performance penalty in favor of ease of use. The alternative
 * would be using a 'Map/List wrapper that delegates calls to abstract methods in the interface
 * (I won't do this just now).
 *
 * HTTP request context. It holds client supplied data and methods to change the response.
 */
abstract class Request {
    /**
     * Provides the HTTP method of the request.
     */
    val method: Method by lazy { method() }

    /**
     * Provides the name of the scheme used to make this request.
     */
    val scheme: String by lazy { scheme() }

    /**
     * Provides the fully qualified name of the client.
     */
    val host: String by lazy { host() }

    /**
     * Provides the client IP address.
     */
    val ip: String by lazy { ip() }

    /**
     * Provides the port number used to make the request.
     */
    val port: Int by lazy { port() }

    /**
     * Provides the servlet path of the request.
     */
    val path: String by lazy { path() }

    /**
     * Provides the query string of the request.
     */
    val queryString: String by lazy { queryString() }

    /**
     * Provides the URL client used to make the request.
     */
    val url: String by lazy { url() }

    /**
     * Provides a [Map] of multipart parts in the request.
     */
    val parts: Map<String, Part> by lazy { parts() }

    /**
     * Provides [Map] of parsed key-value pairs of query parameters in the request.
     */
    val queryParametersValues: Map<String, List<String>> by lazy { queryParameters() }

    /**
     * Provides [Map] of request parameters contained in form fields.
     */
    val formParametersValues: Map<String, List<String>> by lazy { formParameters() }

    /**
     * Provides a [Map] of first values of all query parameters in the request.
     */
    val queryParameters: Map<String, String> by lazy { firsts(queryParametersValues) }

    /**
     * Provides a [Map] of first values of all form fields.
     */
    val formParameters: Map<String, String> by lazy { firsts(formParametersValues) }

    /**
     * Provides a [Map] of all path parameters.
     */
    val pathParameters: Map<String, String> by lazy { pathParameters() }

    /**
     * Provides a [List] of certificate chain used for SSL.
     */
    val certificateChain: List<X509Certificate> by lazy { certificateChain() }

    /**
     * Check if the request is secure.
     */
    val secure: Boolean by lazy { scheme == "https" }

    /**
     * [List] of parameters for "Accept" header key.
     */
    val acceptValues: List<String> by lazy { headersValues["Accept"] ?: emptyList() }

    /**
     * First value in "Accept" header key.
     */
    val accept: String? by lazy { acceptValues.firstOrNull() }

    /**
     * Preferred content-type specified under "Accept" header key.
     */
    val preferredType: String? by lazy { accept }

    /**
     * First value of "User-Agent" header key.
     */
    val userAgent: String? by lazy { headersValues["User-Agent"]?.firstOrNull() }

    /**
     * First value of "Referer" header key.
     */
    val referer: String? by lazy { headersValues["Referer"]?.firstOrNull() }

    /**
     * First value of "Origin" header key.
     */
    val origin: String? by lazy { headersValues["Origin"]?.firstOrNull() }

    /**
     * First certificate in the [certificateChain].
     */
    val certificate: X509Certificate? by lazy { certificateChain.firstOrNull() }

    /**
     * Request body sent by the client.
     */
    val body: String by lazy { loadBody() }

    /**
     * [Map] of header values of the request.
     */
    val headersValues: Map<String, List<String>> by lazy { headers() }

    /**
     * [Map] of first values of the headers of the request.
     */
    val headers: Map<String, String> by lazy { firsts(headersValues) }

    /**
     * [Map] of the cookies contained in the browser.
     */
    val cookies: Map<String, HttpCookie> by lazy { cookies() }

    /**
     * Content type of the request body.
     */
    val contentType: String? by lazy { contentType() }

    /**
     * Length of the request body.
     */
    val contentLength: Long by lazy { contentLength() }

    /**
     * Parses request body according to given [type].
     *
     * @param type Class specifying the type to which the body is to be parsed
     */
    fun <T : Any> body(type: KClass<T>): T = body.parse(type, requestFormat())

    /**
     * Parses request body objects to given [type].
     *
     * @param type Class specifying the type to which the objects are to be parsed
     */
    fun <T : Any> bodyObjects(type: KClass<T>): List<T> = body.parseObjects(type, requestFormat())

    /** Parses request body. */
    inline fun <reified T : Any> body(): T = body(T::class)

    /** Parses request body objects. */
    inline fun <reified T : Any> bodyObjects(): List<T> = bodyObjects(T::class)

    protected abstract fun method(): Method                      // "GET"
    protected abstract fun scheme(): String                      // "http"
    protected abstract fun host(): String                        // "example.com"
    protected abstract fun ip(): String                          // client IP address
    protected abstract fun port(): Int                           // 80
    protected abstract fun path(): String                        // "/foo" servlet path + path info
    protected abstract fun pathParameters(): Map<String, String> // ["p"] "p" path parameter
    protected abstract fun queryString(): String                 // ""
    protected abstract fun url(): String                         // "http://example.com/example/foo"
    protected abstract fun parts(): Map<String, Part>            // hash of multipart parts
    protected abstract fun queryParameters(): Map<String, List<String>>
    protected abstract fun formParameters(): Map<String, List<String>>
    protected abstract fun certificateChain(): List<X509Certificate>

    protected abstract fun loadBody(): String                    // request body sent by the client
    protected abstract fun headers(): Map<String, List<String>>  // ["H"] // value of "H" header
    protected abstract fun cookies(): Map<String, HttpCookie>    // hash of browser cookies
    protected abstract fun contentType(): String?                // media type of request.body
    protected abstract fun contentLength(): Long                 // length of request.body

    internal fun requestType(): String =
        contentType ?: defaultFormat.contentType

    internal fun requestFormat(): SerializationFormat =
        SerializationManager.formatOf(requestType())

    private fun <K, V> firsts(map: Map<K, List<V>>): Map<K, V> =
        map.mapValues { it.value.first() }
}

/**
 * This function aggregates path parameters, form parameters and query parameters into a map and
 * convert it into given class using object mapper
 * Usage : request.parseAllParameters(MyCustomDataClass::class)
 *
 * @param type is the KotlinClass of type T (where T can be any class eg:MyCustomDataClass)
 * @return an object of type T  (eg: MyCustomDataClass())
 */
fun <T : Any> Request.parseAllParameters(type: KClass<T>): T? {
    val requestMap = generateRequestMap(this)
    return try {
        requestMap.convertToObject(type)
    }
    catch (iae: IllegalArgumentException) {
        logger.warn { "Unable to parse request data into ${type.simpleName} : ${iae.message}" }
        null
    }
}

/**
 * Function used to aggregate all request parameters and create a map object out of it
 * @param request : an inherited instance of com.hexagonkt.http.server.Request
 * @return a map containing all request parameters
 */
private fun generateRequestMap(request: Request): Map<String, Any> {
    val requestMap: MutableMap<String, Any> = hashMapOf()
    requestMap.putAll(transformValues(request.formParametersValues))
    requestMap.putAll(transformValues(request.queryParametersValues))
    requestMap.putAll(request.pathParameters)
    return requestMap
}

/**
 * Function will check the list of values against each key. Returns the value alone if the list size
 * is 1 else returns the entire list as is
 * @param stringToListMap : Map<String,List<String>> type
 * @return stringToAnyMap : Map<String,Any> type
 */
private fun transformValues(stringToListMap: Map<String, List<String>>): MutableMap<String, Any> {
    val stringToAnyMap = mutableMapOf<String, Any>()
    for (queryParameter in stringToListMap.entries) {
        val value: Any = when (queryParameter.value.size) {
            1 -> queryParameter.value.first()
            else -> queryParameter.value
        }
        stringToAnyMap[queryParameter.key] = value
    }
    return stringToAnyMap
}

