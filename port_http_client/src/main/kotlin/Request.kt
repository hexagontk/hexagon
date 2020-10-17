package com.hexagonkt.http.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.hexagonkt.helpers.logger
import com.hexagonkt.http.Method
import com.hexagonkt.http.Part
import com.hexagonkt.http.Path
import kotlin.reflect.KClass

/**
 * HTTP request send to the server.
 */
data class Request(
    val method: Method,
    val path: Path,
    val body: Any? = null,
    val headers: Map<String, List<String>> = emptyMap(),
    val pathParameters: Map<String, List<String>> = emptyMap(),
    val formParameters: Map<String, List<String>> = emptyMap(),
    val parts: Map<String, Part> = emptyMap(),
    val contentType: String? = null
) {
    constructor(
        method: Method,
        path: String,
        body: Any? = null,
        headers: Map<String, List<String>> = emptyMap(),
        pathParameters: Map<String, List<String>> = emptyMap(),
        formParameters: Map<String, List<String>> = emptyMap(),
        parts: Map<String, Part> = emptyMap(),
        contentType: String? = null
    ) : this (
        method,
        Path(path),
        body,
        headers,
        pathParameters,
        formParameters,
        parts,
        contentType
    )
}

/**
 * This function aggregates path parameters, form parameters and query parameters into a map and convert it into given class using object mapper
 * Usage : request.parseAllParameters(MyCustomDataClass::class)
 *
 * @param type is the KotlinClass of type T (where T can be any class eg:MyCustomDataClass)
 * @return an object of type T  (eg: MyCustomDataClass())
 */
fun <T : Any> Request.parseAllParameters(type: KClass<T>): T? {
    val requestMap = generateRequestMap(this)
    return try {
        jacksonObjectMapper().convertValue(requestMap, type.javaObjectType)
    }
    catch (iae: IllegalArgumentException) {
        logger.warn { "Unable to parse request data into ${type.simpleName} : ${iae.message}" }
        null
    }
}

/**
 * inline function used to aggreate all request parameters and create a map object out of it
 * @param request : an object of class com.hexagonkt.http.client.Request
 * @return a map containing all request paremeters
 */
private inline fun generateRequestMap(request: Request): Map<String, Any> {
    val requestMap: MutableMap<String, Any> = hashMapOf()
    for (entry in request.pathParameters.entries) {
        requestMap.put(entry.key, entry.value.first())
    }
    requestMap.putAll(request.formParameters)
    return requestMap
}
