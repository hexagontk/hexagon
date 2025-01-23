package com.hexagontk.http.handlers

import com.hexagontk.handlers.Context
import com.hexagontk.http.basicAuth
import com.hexagontk.http.model.*
import com.hexagontk.http.parseQueryString
import com.hexagontk.http.patterns.PathPattern
import kotlin.jvm.javaClass
import kotlin.test.assertEquals

private fun assertEqualPathPatterns(a: PathPattern, b: PathPattern) {
    if (a.javaClass != b.javaClass) assert(false)
    if (a.pattern != b.pattern) assert(false)
    if (a.prefix != b.prefix) assert(false)
}

internal fun assertEqualHttpPredicates(a: HttpPredicate, b: HttpPredicate) {
    if (a.javaClass != b.javaClass) assert(false)
    if (a.methods != b.methods) assert(false)
    if (a.status != b.status) assert(false)

    assertEqualPathPatterns(a.pathPattern, b.pathPattern)
}

internal fun assertEqualHttpPredicatesFn(
    a: (Context<HttpCall>) -> Boolean, b: (Context<HttpCall>) -> Boolean
) {
    if (a.javaClass != b.javaClass) assert(false)

    a as HttpPredicate
    b as HttpPredicate

    if (a.methods != b.methods) assert(false)
    if (a.status != b.status) assert(false)

    assertEqualPathPatterns(a.pathPattern, b.pathPattern)
}

internal fun assertEqualPathHandlers(a: PathHandler, b: PathHandler) {
    if (a.javaClass != b.javaClass) assert(false)
    if (a.handlers != b.handlers) assert(false)
    assertEqualHttpPredicates(a.handlerPredicate, b.handlerPredicate)
}

internal fun assertEqualHttpPredicatesList(a: List<HttpPredicate>, b: List<HttpPredicate>) {
    assertEquals(a.size, b.size)
    for (i in a.indices)
        assertEqualHttpPredicates(a[i], b[i])
}

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

internal fun HttpRequestPort.auth(user: String? = null, password: String? = null): HttpRequestPort {
    val authorization =
        if (user != null || password != null)
            basicAuth(user ?: "", password ?: "")
        else
            null

    return if (authorization != null)
        with(authorization = Authorization("Basic", authorization))
    else
        this
}

internal fun assertResponseEquals(
    response: HttpResponsePort?, content: String, status: Int = OK_200) {

    assertEquals(status, response?.status)
    assertEquals(content, response?.body?.let { it as String })
}
