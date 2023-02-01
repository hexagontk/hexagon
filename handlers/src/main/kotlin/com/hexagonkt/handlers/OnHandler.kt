package com.hexagonkt.handlers

data class OnHandler<T : Any>(
    override val predicate: Predicate<T> = { true },
    val beforeCallback: Callback<T>,
) : Handler<T> {

    override val callback: Callback<T> = {
        try {
            beforeCallback(it).next()
        }
        catch (e: Exception) {
            it.with(exception = e).next()
        }
    }
}
