#!/usr/bin/env bash

# https://tools.ietf.org/html/rfc2606

rm -f ca.* server*.*

# Create CA (root) key pair:
keytool -genkeypair \
 -keystore ca.p12 \
 -storetype pkcs12 \
 -storepass hexagon \
 -alias ca \
 -validity 3650 \
 -ext bc:ca:true \
 -keyalg RSA \
 -dname "CN=Hexagon TEST Root CA,O=Hexagon,C=ES"

# Export CA certificate
keytool -exportcert \
 -keystore ca.p12 \
 -storetype pkcs12 \
 -storepass hexagon \
 -alias ca \
 -rfc \
 -file ca.pem

# Replace server certificate
keytool -importcert \
 -keystore trust_store.p12 \
 -storetype pkcs12 \
 -storepass hexagon \
 -alias ca \
 -file ca.pem \
 -noprompt

# Create server key pair:
keytool -genkeypair \
 -keystore server.p12 \
 -storetype pkcs12 \
 -storepass hexagon \
 -alias server \
 -validity 3650 \
 -keyalg RSA \
 -dname "CN=hexagonkt.test,O=Hexagon,C=ES"

# Server certificate signing request
keytool -certreq \
 -keystore server.p12 \
 -storetype pkcs12 \
 -storepass hexagon \
 -alias server \
 -file server.csr

# Server certificate sign
keytool -gencert \
 -keystore ca.p12 \
 -storetype pkcs12 \
 -storepass hexagon \
 -alias ca \
 -validity 3650 \
 -ext san=dns:api.hexagonkt.test,dns:www.hexagonkt.test \
 -rfc \
 -infile server.csr \
 >server.pem

# Chain certificates
cat ca.pem server.pem >serverchain.pem

# Replace server certificate
keytool -importcert \
 -keystore server.p12 \
 -storetype pkcs12 \
 -storepass hexagon \
 -alias server \
 -file serverchain.pem \
 -noprompt
