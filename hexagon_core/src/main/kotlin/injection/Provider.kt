package com.hexagonkt.injection

sealed class Provider<out T : Any> {
    abstract fun provide(): T

    data class Generator<out T : Any>(val generator: () -> T) : Provider<T>() {
        override fun provide(): T =
            generator()
    }

    data class Instance<out T : Any>(val instance: T) : Provider<T>() {
        override fun provide(): T =
            instance
    }
}
