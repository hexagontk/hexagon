package com.hexagonkt.http.model

// TODO Add 'path', 'httpOnly', 'sameSite' and 'sameParty'
data class HttpCookie(
    val name: String,
    val value: String,
    val maxAge: Long = -1,
    val secure: Boolean = false,
) {
    init {
        require(name.isNotBlank()) { "Cookie name can not be blank: $name" }
    }

    fun delete(): HttpCookie =
        copy(value = "", maxAge = 0)
}
