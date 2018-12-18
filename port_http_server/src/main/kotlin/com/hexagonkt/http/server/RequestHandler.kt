package com.hexagonkt.http.server

import com.hexagonkt.http.Route

sealed class RequestHandler {
    abstract val route: Route

    data class FilterHandler(
        override val route: Route,
        val order: FilterOrder,
        val callback: FilterCallback) : RequestHandler()

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

    data class AssetsHandler(
        override val route: Route,
        val path: String) : RequestHandler()

    data class PathHandler(
        override val route: Route,
        val router: Router) : RequestHandler()
}
