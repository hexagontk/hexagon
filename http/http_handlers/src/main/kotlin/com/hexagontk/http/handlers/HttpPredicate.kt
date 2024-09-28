package com.hexagontk.http.handlers

import com.hexagontk.core.debug
import com.hexagontk.core.loggerOf
import com.hexagontk.handlers.Context
import com.hexagontk.http.patterns.LiteralPathPattern
import com.hexagontk.http.model.HttpMethod
import com.hexagontk.http.model.HttpStatus
import com.hexagontk.http.patterns.PathPattern
import com.hexagontk.http.patterns.createPathPattern
import com.hexagontk.http.model.HttpCall
import java.lang.System.Logger
import kotlin.reflect.KClass

data class HttpPredicate(
    val methods: Set<HttpMethod> = emptySet(),
    val pathPattern: PathPattern = LiteralPathPattern(),
    val exception: KClass<out Exception>? = null,
    val status: HttpStatus? = null,
) : (Context<HttpCall>) -> Boolean {

    private companion object {
        val logger: Logger = loggerOf(HttpPredicate::class)
    }

    private fun PathPattern.isEmpty(): Boolean =
        pattern.isEmpty()

    val predicate: (Context<HttpCall>) -> Boolean =
        if (methods.isEmpty()) log(::filterWithoutMethod)
        else log(::filterWithMethod)

    constructor(
        methods: Set<HttpMethod> = emptySet(),
        pattern: String = "",
        exception: KClass<out Exception>? = null,
        status: HttpStatus? = null,
        prefix: Boolean = false,
    ) :
        this(methods, createPathPattern(pattern, prefix), exception, status)

    override fun invoke(context: Context<HttpCall>): Boolean =
        predicate(context)

    fun clearMethods(): HttpPredicate =
        copy(methods = emptySet())

    private fun log(
        predicate: (Context<HttpCall>) -> Boolean
    ): (Context<HttpCall>) -> Boolean = {
        val allowed = predicate(it)
        logger.debug { "${describe()} -> ${if (allowed) "ALLOWED" else "DENIED"}" }
        allowed
    }

    private fun filterMethod(context: Context<HttpCall>): Boolean =
        context.event.request.method in methods

    private fun filterPattern(context: Context<HttpCall>): Boolean =
        if (pathPattern.isEmpty() && context.event.request.path == "/") true
        else pathPattern.matches(context.event.request.path)

    private fun filterException(context: Context<HttpCall>): Boolean {
        val exceptionClass = context.exception?.javaClass ?: return false
        return exception?.java?.isAssignableFrom(exceptionClass) ?: false
    }

    private fun filterStatus(context: Context<HttpCall>): Boolean =
        status == context.event.response.status

    private fun filterWithoutMethod(context: Context<HttpCall>): Boolean =
        filterPattern(context)
            && (exception == null || filterException(context))
            && (status == null || filterStatus(context))

    private fun filterWithMethod(context: Context<HttpCall>): Boolean =
        filterMethod(context) && filterWithoutMethod(context)

    fun addPrefix(prefix: String): HttpPredicate =
        copy(pathPattern = pathPattern.addPrefix(prefix))

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
