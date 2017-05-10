package co.there4.hexagon.server

sealed class RequestHandler {
    abstract val route: Route

    data class FilterHandler(
        override val route: Route,
        val order: FilterOrder,
        val handler: FilterCallback) : RequestHandler()

    data class RouteHandler(
        override val route: Route,
        val handler: RouteCallback) : RequestHandler()

    data class ErrorHandler(
        override val route: Route,
        val exception: Class<out Exception>,
        val handler: ExceptionCallback) : RequestHandler()

    data class ErrorCodeHandler(
        override val route: Route,
        val code: Int,
        val handler: ErrorCodeCallback) : RequestHandler()

    data class AssetsHandler(
        override val route: Route,
        val path: String) : RequestHandler()

    data class PathHandler(
        override val route: Route,
        val router: Router) : RequestHandler()
}
