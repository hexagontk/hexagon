package com.hexagonkt.serialization

interface Data<T> {
    fun data(): Map<String, *>
    fun with(data: Map<String, *>): T
}
