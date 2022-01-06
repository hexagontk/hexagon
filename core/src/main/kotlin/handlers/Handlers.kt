package com.hexagonkt.core.handlers

typealias Predicate<T> = suspend (Context<T>) -> Boolean
typealias Callback<T> = suspend (Context<T>) -> Context<T>
