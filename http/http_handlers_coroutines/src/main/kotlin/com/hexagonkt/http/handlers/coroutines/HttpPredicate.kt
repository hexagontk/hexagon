package com.hexagonkt.http.handlers.coroutines

import com.hexagonkt.handlers.coroutines.Context
import com.hexagonkt.core.logging.Logger
import com.hexagonkt.http.patterns.LiteralPathPattern
import com.hexagonkt.http.model.HttpMethod
import com.hexagonkt.http.model.HttpStatus
import com.hexagonkt.http.patterns.PathPattern
import com.hexagonkt.http.patterns.createPathPattern
import com.hexagonkt.http.model.HttpCall
import kotlin.reflect.KClass

data class HttpPredicate(
    val methods: Set<HttpMethod> = emptySet(),
    val pathPattern: PathPattern = LiteralPathPattern(),
    val exception: KClass<out Exception>? = null,
    val status: HttpStatus? = null,
) : (Context<HttpCall>) -> Boolean {

    private companion object {
        val logger: Logger = Logger(HttpPredicate::class)
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
    ): (Context<HttpCall>) -> Boolean {
        return if (logger.isDebugEnabled()) {
            {
                val allowed = predicate(it)
                logger.debug { "${describe()} -> ${if (allowed) "ALLOWED" else "DENIED"}" }
                allowed
            }
        }
        else
            predicate
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
