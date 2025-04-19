package com.hexagontk.injection

/**
 * Manage root module and injector. This object keep tracks of generator functions or specific
 * instances bound to classes. Different providers can be bound to the same type using 'tags'.
 *
 * @property module Root module with the bindings.
 * @property injector Injector created with the Manager's [module].
 */
object InjectionManager {
    val module: Module = Module()
    val injector: Injector = Injector(module)
}
