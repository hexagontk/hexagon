package com.hexagonkt.args.formatter

interface Formatter<T> {
    fun summary(component: T): String
    fun definition(component: T): String
    fun detail(component: T): String
}
