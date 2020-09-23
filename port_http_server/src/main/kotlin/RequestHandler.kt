package com.hexagonkt.http.server

import com.hexagonkt.http.Route
import java.io.File
import java.net.URL

sealed class RequestHandler {
    abstract val route: Route

    data class FilterHandler(
        override val route: Route,
        val order: FilterOrder,
        val callback: RouteCallback) : RequestHandler()

    data class RouteHandler(
        override val route: Route,
        val callback: RouteCallback) : RequestHandler()

    data class ExceptionHandler(
        override val route: Route,
        val exception: Class<out Exception>,
        val callback: ExceptionCallback) : RequestHandler()

    data class CodeHandler(
        override val route: Route,
        val code: Int,
        val callback: ErrorCodeCallback) : RequestHandler()

    data class ResourceHandler(
        override val route: Route,
        val resource: URL) : RequestHandler()

    data class FileHandler(
        override val route: Route,
        val file: File) : RequestHandler()

    data class PathHandler(
        override val route: Route,
        val router: Router) : RequestHandler()
}
