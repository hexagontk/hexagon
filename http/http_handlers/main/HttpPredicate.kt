package com.hexagontk.http.handlers

import com.hexagontk.handlers.Context
import com.hexagontk.http.patterns.LiteralPathPattern
import com.hexagontk.http.model.HttpMethod
import com.hexagontk.http.patterns.PathPattern
import com.hexagontk.http.patterns.createPathPattern
import com.hexagontk.http.model.HttpCall

class HttpPredicate(
    val methods: Set<HttpMethod> = emptySet(),
    val pathPattern: PathPattern = LiteralPathPattern(),
    val status: Int? = null,
) : (Context<HttpCall>) -> Boolean {

    private fun PathPattern.isEmpty(): Boolean =
        pattern.isEmpty()

    val predicate: (Context<HttpCall>) -> Boolean =
        if (methods.isEmpty()) ::filterWithoutMethod else ::filterWithMethod

    constructor(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        status: Int? = null,
        prefix: Boolean = false,
    ) :
        this(methods, createPathPattern(pattern, prefix), status)

    override fun invoke(context: Context<HttpCall>): Boolean =
        predicate(context)

    fun clearMethods(): HttpPredicate =
        HttpPredicate(emptySet(), pathPattern, status)

    private fun filterMethod(context: Context<HttpCall>): Boolean =
        context.event.request.method in methods

    private fun filterPattern(context: Context<HttpCall>): Boolean =
        if (pathPattern.isEmpty() && context.event.request.path == "/") true
        else pathPattern.matches(context.event.request.path)

    private fun filterStatus(context: Context<HttpCall>): Boolean =
        status == context.event.response.status

    private fun filterWithoutMethod(context: Context<HttpCall>): Boolean =
        filterPattern(context)
            && (status == null || filterStatus(context))

    private fun filterWithMethod(context: Context<HttpCall>): Boolean =
        filterMethod(context) && filterWithoutMethod(context)

    fun addPrefix(prefix: String): HttpPredicate =
        HttpPredicate(methods, pathPattern.addPrefix(prefix), status)

    fun describe(): String =
        methods
            .map { it.name }
            .ifEmpty { listOf("ANY") }
            .joinToString(
                separator = ", ",
                postfix = pathPattern.describe().prependIndent(" "),
                transform = { it }
            )
}
