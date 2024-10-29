package com.hexagontk.http.handlers

import com.hexagontk.http.basicAuth
import com.hexagontk.http.model.*
import com.hexagontk.http.parseQueryString
import kotlin.test.assertEquals

internal fun assertResponseContains(
    response: HttpResponsePort?, status: Int, vararg content: String) {

    assertEquals(status, response?.status)
    val payload = response?.body?.let { b -> b as String }
    content.forEach { assert(payload?.contains(it) ?: false) }
}

internal fun assertResponseContains(response: HttpResponsePort?, vararg content: String) {
    assertResponseContains(response, OK_200, *content)
}

internal fun HttpHandler.send(
    method: HttpMethod,
    requestPath: String,
    query: String = "",
    user: String? = null,
    password: String? = null,
): HttpResponsePort =
    process(
        HttpRequest(
            method = method,
            path = requestPath,
            queryParameters = parseQueryString(query)
        )
        .auth(user, password)
    ).response

internal fun HttpRequest.auth(
    user: String? = null, password: String? = null): HttpRequest {

    val authorization =
        if (user != null || password != null)
            basicAuth(user ?: "", password ?: "")
        else
            null

    return if (authorization != null)
        copy(authorization = Authorization("Basic", authorization))
    else
        this
}

internal fun assertResponseEquals(
    response: HttpResponsePort?, content: String, status: Int = OK_200) {

    assertEquals(status, response?.status)
    assertEquals(content, response?.body?.let { it as String })
}
