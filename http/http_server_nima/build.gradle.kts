
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "HTTP server adapter for Helidon Nima (using Java Virtual Threads)."

dependencies {
    val nimaVersion = properties["nimaVersion"]

    "api"(project(":http:http_server"))
    "api"("io.helidon.nima.http2:helidon-nima-http2-webserver:$nimaVersion")
    "api"("io.helidon.nima.http.encoding:helidon-nima-http-encoding-gzip:$nimaVersion")
    "api"("io.helidon.nima.http.media:helidon-nima-http-media-multipart:$nimaVersion")

    "testImplementation"(project(":http:http_test"))
    "testImplementation"(project(":http:http_client_jetty"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
    "testImplementation"(project(":serialization:serialization_jackson_yaml"))
}
