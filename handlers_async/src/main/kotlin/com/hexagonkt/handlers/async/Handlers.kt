package com.hexagonkt.handlers.async

import java.util.concurrent.CompletableFuture

fun <T : Any, Z : Context<T>> Z.done(): CompletableFuture<Z> =
    CompletableFuture.completedFuture(this)
