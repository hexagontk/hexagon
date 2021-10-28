package com.hexagonkt.helpers

fun <K, V> multiMapOf(vararg pairs: Pair<K, List<V>>): MultiMap<K, V> =
    MultiMap(pairs.toMap())

fun <K, V> Map<K, List<V>>.toMultiMap(): MultiMap<K, V> =
    MultiMap(this)
