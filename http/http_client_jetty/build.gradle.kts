
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "HTTP client adapter for Jetty (without WebSockets support)."

dependencies {
    val jettyVersion = properties["jettyVersion"]
    val slf4jVersion = properties["slf4jVersion"]

    "api"(project(":http:http_client"))
    "api"("org.eclipse.jetty.http2:jetty-http2-client-transport:$jettyVersion")

    "testImplementation"("org.slf4j:slf4j-jdk14:$slf4jVersion")
}
