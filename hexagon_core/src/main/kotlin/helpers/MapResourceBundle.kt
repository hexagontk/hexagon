package com.hexagonkt.helpers

import java.util.*

abstract class MapResourceBundle(val map: Map<*, *>) : ListResourceBundle() {

    val data: Array<Array<Any?>> by lazy {
        map.entries.map { arrayOf(it.key, it.value) }.toTypedArray()
    }

    override fun getContents(): Array<Array<Any?>> =
        data
}
