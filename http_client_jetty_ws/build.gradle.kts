
plugins {
    id("java-library")
}

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/native.gradle")

dependencies {
    "api"(project(":http_client_jetty"))
    "api"("org.eclipse.jetty.websocket:websocket-jetty-client")
}
