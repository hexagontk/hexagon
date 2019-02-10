package com.hexagonkt.helpers

class RequiredKeysMap<K, V>(private val map: Map<K, V>) : Map<K, V> by map {
    override operator fun get(key: K): V = map.require(key)
}
