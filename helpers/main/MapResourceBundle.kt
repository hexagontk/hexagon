package com.hexagontk.helpers

import java.util.*

/**
 * .
 *
 * @property map .
 */
abstract class MapResourceBundle(val map: Map<*, *>) : ListResourceBundle() {

    constructor(vararg pairs: Pair<*, *>) : this(pairs.toMap())

    val data: Array<Array<Any?>> by lazy {
        map.entries.map { arrayOf(it.key, it.value) }.toTypedArray()
    }

    override fun getContents(): Array<Array<Any?>> =
        data
}
