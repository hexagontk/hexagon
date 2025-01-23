package com.hexagontk.http.model

import java.time.Instant

/**
 * TODO .
 *
 * TODO Implement HttpField
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
class Cookie(
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
        with(value = "", maxAge = 0)

    fun with(
        name: String = this.name,
        value: String = this.value,
        maxAge: Long = this.maxAge,
        secure: Boolean = this.secure,
        path: String = this.path,
        httpOnly: Boolean = this.httpOnly,
        domain: String? = this.domain,
        sameSite: CookieSameSite? = this.sameSite,
        expires: Instant? = this.expires,
    ) = Cookie(
        name = name,
        value = value,
        maxAge = maxAge,
        secure = secure,
        path = path,
        httpOnly = httpOnly,
        domain = domain,
        sameSite = sameSite,
        expires = expires,
    )
}
