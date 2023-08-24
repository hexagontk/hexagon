package com.hexagonkt.handlers

/**
 * Handler for an event.
 *
 * @param T Event type.
 */
// TODO Add 'parent' in order to ease their use as a tree
interface Handler<T : Any> {
    val predicate: (Context<T>) -> Boolean
    val callback: (Context<T>) -> Context<T>

    fun process(context: Context<T>): Context<T>
}
