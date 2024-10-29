package com.hexagontk.http.model

data class Field(
    override val name: String,
    override val value: Any? = null,
) : HttpField {

    override val text: String = value?.toString() ?: ""
}
