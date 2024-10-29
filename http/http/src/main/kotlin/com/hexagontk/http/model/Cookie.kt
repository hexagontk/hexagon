package com.hexagontk.http.model

import java.time.Instant

/**
 * TODO .
 *
 * TODO Implement HttpHeader
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
        copy(value = "", maxAge = 0)

    fun copy(
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Cookie

        if (name != other.name) return false
        if (value != other.value) return false
        if (maxAge != other.maxAge) return false
        if (secure != other.secure) return false
        if (path != other.path) return false
        if (httpOnly != other.httpOnly) return false
        if (domain != other.domain) return false
        if (sameSite != other.sameSite) return false
        if (expires != other.expires) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + maxAge.hashCode()
        result = 31 * result + secure.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + httpOnly.hashCode()
        result = 31 * result + (domain?.hashCode() ?: 0)
        result = 31 * result + (sameSite?.hashCode() ?: 0)
        result = 31 * result + (expires?.hashCode() ?: 0)
        return result
    }
}
