package com.hexagonkt.http.server.security

import com.hexagonkt.helpers.decodeBase64
import com.hexagonkt.helpers.encodeToBase64
import com.hexagonkt.http.server.Call

interface AuthenticatorPort {
    fun authenticate(call: Call): Principal?
}

interface Principal

class BasicAuthenticator(val credentialSupplier: Call.(String) -> String) : AuthenticatorPort {

    private fun basicAuthorization(user: String, password: String): String =
        "Basic " + "$user:$password".encodeToBase64()

    override fun authenticate(call: Call): Principal? {

        val authorization = call.request.headers["Authorization"] ?: call.halt(401)

        check(authorization.startsWith("Basic ")) { "" }
        check(authorization.length > "Basic ".length) { "" }

        val (user, password) = String(authorization.removePrefix("Basic ").decodeBase64())
            .split(":")

        val serverPassword = call.credentialSupplier(user)
        val serverAuthorization = basicAuthorization(user, password)

        if (authorization != serverAuthorization)
            call.halt(403)

        return object : Principal {}
    }
}
