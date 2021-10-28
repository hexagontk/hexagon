package com.hexagonkt.http

data class Cookie(
    val name: String,
    val value: String,
    val maxAge: Long = -1,
    val secure: Boolean = false,
)
