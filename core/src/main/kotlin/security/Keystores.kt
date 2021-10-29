package com.hexagonkt.core.security

import java.net.URL
import java.security.KeyStore
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

fun loadKeyStore(resource: URL, password: String): KeyStore =
    KeyStore.getInstance("PKCS12").apply {
        load(resource.openStream(), password.toCharArray())
    }

fun KeyStore.getPrivateKey(alias: String, password: String): RSAPrivateKey =
    this.getKey(alias, password.toCharArray()) as RSAPrivateKey

fun KeyStore.getPublicKey(alias: String): RSAPublicKey =
    this.getCertificate(alias).publicKey as RSAPublicKey
