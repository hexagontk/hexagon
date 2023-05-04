package com.hexagonkt.http.model

import java.time.Instant

data class Cookie(
    val name: String,
    val value: String,
    val maxAge: Long = -1,
    val secure: Boolean = false,
    val path: String = "/",
    val httpOnly: Boolean = true,
    val domain: String = "",
    val sameSite: Boolean = true,
    val expires: Instant? = null,
) {
    init {
        require(name.isNotBlank()) { "Cookie name can not be blank: $name" }
    }

    fun delete(): Cookie =
        copy(value = "", maxAge = 0)
}
