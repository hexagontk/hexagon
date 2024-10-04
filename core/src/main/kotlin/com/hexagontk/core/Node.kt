package com.hexagontk.core

// TODO Other possible names: Struct or Record
internal class Node(val pairs: List<Pair<String?, *>>) : List<Pair<String?, *>> by pairs {
    val list: Boolean by lazy { pairs.all { it.first == null } }
    val map: Boolean by lazy { !list }

//    constructor(items: List<*>) : this(items.map { null to it })
    constructor(items: Map<String, *>) : this(items.map { (k, v) -> k to v })
//    constructor(vararg items: Any) : this(items.asList())
    constructor(vararg items: Pair<String, *>) : this(items.toMap())

    private fun asMap(): Map<String?, *> =
        pairs.toMap() // Keeps last if duplicated key

    private fun groupMap(): Map<String?, *> =
        pairs.groupBy { it.first }
}


internal fun f() {
    Node(
        mapOf("a" to 1)
    )

//    Node(
//        listOf("a", 1)
//    )

    Node("a" to 1)

//    Node("a", 1)
//
//    Node(
//        "a",
//        1,
//        Node(
//            "a" to "b",
//            "b" to true
//        )
//    )
}
