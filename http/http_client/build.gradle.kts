
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")

if (findProperty("fullBuild") != null) {
    apply(from = "$rootDir/gradle/publish.gradle")
    apply(from = "$rootDir/gradle/dokka.gradle")
    apply(from = "$rootDir/gradle/native.gradle")
    apply(from = "$rootDir/gradle/detekt.gradle")
}

description =
    "HTTP client supporting SSL, cookies, WebSockets, and HTTP/2. Requires an adapter to be used."

dependencies {
    "api"(project(":http:http_handlers"))
}
