package com.hexagonkt.core.handlers

data class FilterHandler<T : Any>(
    override val predicate: Predicate<T> = { true },
    override val callback: Callback<T>,
) : Handler<T>
