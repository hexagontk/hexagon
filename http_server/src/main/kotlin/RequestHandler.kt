package com.hexagonkt.http.server

import com.hexagonkt.http.Route
import java.io.File
import java.net.URL

/**
 * Base class for request handlers.
 */
sealed class RequestHandler {
    abstract val route: Route

    /**
     * Handler for given [FilterOrder] for executing the [callback].
     */
    data class FilterHandler(
        override val route: Route,
        val order: FilterOrder,
        val callback: RouteCallback) : RequestHandler()

    /**
     * Handler for routes on HTTP methods.
     */
    data class RouteHandler(
        override val route: Route,
        val callback: RouteCallback) : RequestHandler()

    /**
     * Handler for exceptions on the given route.
     */
    data class ExceptionHandler(
        override val route: Route,
        val exception: Class<out Exception>,
        val callback: ExceptionCallback) : RequestHandler()

    /**
     * Handler for a given status code on the given route.
     */
    data class CodeHandler(
        override val route: Route,
        val code: Int,
        val callback: ErrorCodeCallback) : RequestHandler()

    /**
     * Handler for URL resources.
     */
    data class ResourceHandler(
        override val route: Route,
        val resource: URL) : RequestHandler()

    /**
     * Handler for file.
     */
    data class FileHandler(
        override val route: Route,
        val file: File) : RequestHandler()

    /**
     * Handler for nested routes.
     */
    data class PathHandler(
        override val route: Route,
        val router: Router) : RequestHandler()
}
