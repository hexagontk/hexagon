
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

dependencies {
    val nimaVersion = properties["nimaVersion"]

    "api"(project(":http_server"))
    "api"("io.helidon.nima.http2:helidon-nima-http2-webserver:$nimaVersion")

    "testImplementation"(project(":http_test"))
    "testImplementation"(project(":http_client_jetty"))
    "testImplementation"(project(":serialization_jackson_json"))
    "testImplementation"(project(":serialization_jackson_yaml"))
}
