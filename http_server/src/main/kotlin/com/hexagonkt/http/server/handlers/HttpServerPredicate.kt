package com.hexagonkt.http.server.handlers

import com.hexagonkt.handlers.Context
import com.hexagonkt.handlers.Predicate
import com.hexagonkt.core.logging.Logger
import com.hexagonkt.http.patterns.LiteralPathPattern
import com.hexagonkt.http.model.HttpMethod
import com.hexagonkt.http.model.HttpStatus
import com.hexagonkt.http.patterns.PathPattern
import com.hexagonkt.http.patterns.createPathPattern
import com.hexagonkt.http.server.model.HttpServerCall
import kotlin.reflect.KClass

data class HttpServerPredicate(
    val methods: Set<HttpMethod> = emptySet(),
    val pathPattern: PathPattern = LiteralPathPattern(),
    val exception: KClass<out Exception>? = null,
    val status: HttpStatus? = null,
) : Predicate<HttpServerCall> {

    private val logger: Logger = Logger(HttpServerPredicate::class)

    private fun PathPattern.isEmpty(): Boolean =
        pattern.isEmpty()

    val predicate: Predicate<HttpServerCall> =
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

    override fun invoke(context: Context<HttpServerCall>): Boolean =
        predicate(context)

    fun clearMethods(): HttpServerPredicate =
        copy(methods = emptySet())

    private fun log(predicate: Predicate<HttpServerCall>): Predicate<HttpServerCall> {
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

    private fun filterMethod(context: Context<HttpServerCall>): Boolean =
        context.event.request.method in methods

    private fun filterPattern(context: Context<HttpServerCall>): Boolean =
        if (pathPattern.isEmpty() && context.event.request.path == "/") true
        else pathPattern.matches(context.event.request.path)

    private fun filterException(context: Context<HttpServerCall>): Boolean {
        val exceptionClass = context.exception?.javaClass ?: return false
        return exception?.java?.isAssignableFrom(exceptionClass) ?: false
    }

    private fun filterStatus(context: Context<HttpServerCall>): Boolean =
        status == context.event.response.status

    private fun filterWithoutMethod(context: Context<HttpServerCall>): Boolean =
        filterPattern(context)
            && (exception == null || filterException(context))
            && (status == null || filterStatus(context))

    private fun filterWithMethod(context: Context<HttpServerCall>): Boolean =
        filterMethod(context) && filterWithoutMethod(context)

    fun addPrefix(prefix: String): HttpServerPredicate =
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
