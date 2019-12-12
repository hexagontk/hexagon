#!/usr/bin/env bash

# TLD for local environments https://tools.ietf.org/html/rfc2606

rm -f ./*.p12 ./*.pem ./*.csr

# Create CA (root) key pair
keytool -genkeypair \
 -keystore ca.p12 \
 -storetype pkcs12 \
 -storepass hexagon \
 -alias ca \
 -validity 3650 \
 -ext bc:ca:true \
 -keyalg RSA \
 -dname "CN=Hexagon TEST Root CA,O=Hexagon,C=ES"

# Export CA certificate (PEM)
keytool -exportcert \
 -keystore ca.p12 \
 -storetype pkcs12 \
 -storepass hexagon \
 -alias ca \
 -rfc \
 -file ca.pem

# Create trust store with CA certificate (PEM)
keytool -importcert \
 -keystore trust_store.p12 \
 -storetype pkcs12 \
 -storepass hexagon \
 -alias ca \
 -file ca.pem \
 -noprompt

# Create server key pair
keytool -genkeypair \
 -keystore identity_store.p12 \
 -storetype pkcs12 \
 -storepass hexagon \
 -alias identity \
 -validity 3650 \
 -keyalg RSA \
 -dname "CN=hexagonkt.test,O=Hexagon,C=ES"

# Server certificate signing request
keytool -certreq \
 -keystore identity_store.p12 \
 -storetype pkcs12 \
 -storepass hexagon \
 -alias identity \
 -ext san=dns:api.hexagonkt.test,dns:www.hexagonkt.test,dns:localhost \
 -file identity.csr

# Server certificate sign
keytool -gencert \
 -keystore ca.p12 \
 -storetype pkcs12 \
 -storepass hexagon \
 -alias ca \
 -validity 3650 \
 -ext san=dns:api.hexagonkt.test,dns:www.hexagonkt.test,dns:localhost \
 -rfc \
 -infile identity.csr \
 >identity.pem

# Chain certificates
cat ca.pem identity.pem >identity_chain.pem

# Replace server certificate
keytool -importcert \
 -keystore identity_store.p12 \
 -storetype pkcs12 \
 -storepass hexagon \
 -alias identity \
 -file identity_chain.pem \
 -noprompt

# Clean up
rm ./*.pem ./*.csr
