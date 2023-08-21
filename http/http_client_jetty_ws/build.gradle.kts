
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "HTTP client adapter for Jetty (with WebSockets support)."

dependencies {
    val jettyVersion = properties["jettyVersion"]

    "api"(project(":http:http_client_jetty"))
    "api"("org.eclipse.jetty.websocket:jetty-websocket-jetty-client:$jettyVersion")
}
