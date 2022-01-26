package com.hexagonkt.core.handlers

typealias Predicate<T> = (Context<T>) -> Boolean
typealias Callback<T> = (Context<T>) -> Context<T>
