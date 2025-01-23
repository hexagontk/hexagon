package com.hexagontk.http.model

import com.hexagontk.core.require

abstract class HttpFields(
    val fields: List<HttpField>,
    protected val keyMapper: (String) -> String,
) : List<HttpField> by fields {

    val all: Map<String, List<HttpField>> by lazy { fields.groupBy { it.name.let(keyMapper) } }
    val values: Map<String, HttpField> by lazy {
        fields.reversed().associateBy { it.name.let(keyMapper) }
    }

    val keys: Set<String> by lazy { fields.map { it.name }.toSet() }

    operator fun get(key: String): HttpField? =
        values[key.let(keyMapper)]

    fun getText(key: String): String? =
        get(key)?.text

    fun getAll(key: String): List<HttpField> =
        all[key.let(keyMapper)] ?: emptyList()

    fun getTexts(key: String): List<String> =
        getAll(key).map { it.text }

    fun require(key: String): HttpField =
        values.require(key.let(keyMapper))

    fun requireText(key: String): String =
        require(key).text
}
