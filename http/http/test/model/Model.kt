package com.hexagontk.http.model

import kotlin.jvm.javaClass
import kotlin.test.assertEquals

// TODO Move this testing utilities to a `http_test` module if they are C&P in other places

internal fun assertEqualCookies(a: Cookie, b: Any?) {
    if (a.javaClass != b?.javaClass) assert(false)

    b as Cookie

    if (a.name != b.name) assert(false)
    if (a.value != b.value) assert(false)
    if (a.maxAge != b.maxAge) assert(false)
    if (a.secure != b.secure) assert(false)
    if (a.path != b.path) assert(false)
    if (a.httpOnly != b.httpOnly) assert(false)
    if (a.domain != b.domain) assert(false)
    if (a.sameSite != b.sameSite) assert(false)
    if (!(a.expires?.equals(b.expires) ?: (b.expires == null))) assert(false)
}

internal fun assertEqualHttpRequests(a: HttpRequestPort, b: Any?) {
    if (a.javaClass != b?.javaClass) assert(false)

    b as HttpRequest

    if (a.method != b.method) assert(false)
    if (a.protocol != b.protocol) assert(false)
    if (a.host != b.host) assert(false)
    if (a.port != b.port) assert(false)
    if (a.path != b.path) assert(false)
    if (a.body != b.body) assert(false)
    if (a.contentType?.text != b.contentType?.text) assert(false)
    if (a.certificateChain != b.certificateChain) assert(false)
    if (a.contentLength != b.contentLength) assert(false)
    if (a.authorization != b.authorization) assert(false)

    a.cookies.forEachIndexed { index, cookie ->
        assertEqualCookies(cookie, b.cookies[index])
    }
    a.parts.forEachIndexed { index, part ->
        assertEqualHttpParts(part, b.parts[index])
    }
    a.accept.forEachIndexed { index, accept ->
        assertEquals(accept.text, b.accept[index].text)
    }

    assertEqualHttpFields(a.queryParameters, b.queryParameters)
    assertEqualHttpFields(a.headers, b.headers)
    assertEqualHttpFields(a.formParameters, b.formParameters)
}

internal fun assertEqualHttpResponses(a: HttpResponsePort, b: Any?) {
    if (a.javaClass != b?.javaClass) assert(false)

    b as HttpResponse

    if (a.body != b.body) assert(false)
    if (a.contentType?.text != b.contentType?.text) assert(false)
    if (a.status != b.status) assert(false)
    if (a.reason != b.reason) assert(false)
    if (a.contentLength != b.contentLength) assert(false)
    if (a.onConnect != b.onConnect) assert(false)
    if (a.onBinary != b.onBinary) assert(false)
    if (a.onText != b.onText) assert(false)
    if (a.onPing != b.onPing) assert(false)
    if (a.onPong != b.onPong) assert(false)
    if (a.onClose != b.onClose) assert(false)

    a.cookies.forEachIndexed { index, cookie ->
        assertEqualCookies(cookie, b.cookies[index])
    }

    assertEqualHttpFields(a.headers, b.headers)
}

internal fun assertEqualHttpParts(a: HttpPart, b: Any?) {
    if (a.javaClass != b?.javaClass) assert(false)

    b as HttpPart

    if (a.name != b.name) assert(false)
    if (a.body != b.body) assert(false)
    if (a.contentType?.text != b.contentType?.text) assert(false)
    if (a.size != b.size) assert(false)
    if (a.submittedFileName != b.submittedFileName) assert(false)

    assertEqualHttpFields(a.headers, b.headers)
}

internal fun assertEqualFields(a: HttpField, b: Any?) {
    if (a.javaClass != b?.javaClass) assert(false)

    b as HttpField

    if (a.name != b.name) assert(false)
    if (a.value != b.value) assert(false)
}

internal inline fun <reified T : HttpFields> assertEqualHttpFields(a: T, b: Any?) {
    if (a.javaClass != b?.javaClass) assert(false)

    b as T

    a.fields.forEachIndexed { i, field ->
        assertEqualFields(field, b.fields[i])
    }
}

internal inline fun <reified T : HttpField> assertEqualHttpFieldLists(a: List<T>, b: List<*>) {
    a.forEachIndexed { i, field ->
        assertEqualFields(field, b[i])
    }
}
