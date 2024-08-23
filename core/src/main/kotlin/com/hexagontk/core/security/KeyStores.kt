package com.hexagontk.core.security

import java.net.URL
import java.security.KeyStore
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManagerFactory

// TODO Create CAs and PKs like `certificates.gradle` Check: https://www.baeldung.com/java-keystore
fun loadKeyStore(resource: URL, password: String): KeyStore =
    KeyStore.getInstance("PKCS12").apply {
        load(resource.openStream(), password.toCharArray())
    }

fun KeyStore.getPrivateKey(alias: String, password: String): RSAPrivateKey =
    this.getKey(alias, password.toCharArray()) as RSAPrivateKey

fun KeyStore.getPublicKey(alias: String): RSAPublicKey =
    this.getCertificate(alias).publicKey as RSAPublicKey

fun createTrustManagerFactory(
    resource: URL,
    password: String,
    algorithm: String = TrustManagerFactory.getDefaultAlgorithm()
): TrustManagerFactory {
    val trustStore = loadKeyStore(resource, password)
    val trustManager = TrustManagerFactory.getInstance(algorithm)
    trustManager.init(trustStore)
    return trustManager
}

fun createKeyManagerFactory(
    resource: URL,
    password: String,
    algorithm: String = KeyManagerFactory.getDefaultAlgorithm()
): KeyManagerFactory {
    val keyStore = loadKeyStore(resource, password)
    val keyManager = KeyManagerFactory.getInstance(algorithm)
    keyManager.init(keyStore, password.toCharArray())
    return keyManager
}
