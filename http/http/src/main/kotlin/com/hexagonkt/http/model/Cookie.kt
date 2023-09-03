package com.hexagonkt.http.model

import java.time.Instant

/**
 * TODO .
 *
 * @property name
 * @property value
 * @property maxAge '-1' is the same as empty
 * @property secure
 * @property path '/' is the same as empty
 * @property httpOnly
 * @property domain
 * @property sameSite
 * @property expires
 */
data class Cookie(
    val name: String,
    val value: String = "",
    val maxAge: Long = -1,
    val secure: Boolean = false,
    val path: String = "/",
    val httpOnly: Boolean = false,
    val domain: String? = null,
    val sameSite: CookieSameSite? = null,
    val expires: Instant? = null,
) {
    val deleted: Boolean by lazy { value == "" && maxAge <= 0L }

    init {
        require(name.isNotBlank()) { "Cookie name can not be blank: $name" }
    }

    fun delete(): Cookie =
        copy(value = "", maxAge = 0)
}
