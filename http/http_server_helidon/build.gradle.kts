
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "HTTP server adapter for Helidon (using Java Virtual Threads)."

dependencies {
    val helidonVersion = properties["helidonVersion"]

    "api"(project(":http:http_server"))
    "api"("io.helidon.webserver:helidon-webserver-http2:$helidonVersion")
    "api"("io.helidon.http.encoding:helidon-http-encoding-gzip:$helidonVersion")
    "api"("io.helidon.http.media:helidon-http-media-multipart:$helidonVersion")

    "testImplementation"(project(":http:http_test"))
    "testImplementation"(project(":http:http_client_jetty"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
    "testImplementation"(project(":serialization:serialization_jackson_yaml"))
}
