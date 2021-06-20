package com.hexagonkt.injection

import com.hexagonkt.logging.Logger

/**
 * Providers module and injector. This object keep tracks of generator functions or specific
 * instances bound to classes. Different providers can be bound to the same type using 'tags'.
 */
object InjectionManager {

    internal val logger: Logger by lazy { Logger(this::class) }

    val module: Module = Module()
    val injector: Injector = Injector(module)
}
