
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
    val slf4jVersion = properties["slf4jVersion"]

    "api"(project(":http:http_client_jetty"))
    "api"("org.eclipse.jetty.websocket:jetty-websocket-jetty-client:$jettyVersion")

    "testImplementation"("org.slf4j:log4j-over-slf4j:$slf4jVersion")
    "testImplementation"("org.slf4j:jcl-over-slf4j:$slf4jVersion")
    "testImplementation"("org.slf4j:slf4j-jdk14:$slf4jVersion")
}
