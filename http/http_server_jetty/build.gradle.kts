
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "HTTP server adapter for Jetty (using Servlets under the hood)."

dependencies {
    val jettyVersion = properties["jettyVersion"]

    "api"(project(":http:http_server_servlet"))
    "api"("org.eclipse.jetty.ee10:jetty-ee10-servlet:$jettyVersion")
    "api"("org.eclipse.jetty.http2:jetty-http2-server:$jettyVersion")
    "api"("org.eclipse.jetty:jetty-alpn-java-server:$jettyVersion")

    "testImplementation"(project(":http:http_test"))
    "testImplementation"(project(":http:http_client_jetty_ws"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
    "testImplementation"(project(":serialization:serialization_jackson_yaml"))
    "testImplementation"(
        "org.eclipse.jetty.ee10.websocket:jetty-ee10-websocket-jakarta-server:$jettyVersion"
    )
}
