
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")

description = "HTTP client adapter for Jetty (without WebSockets support)."

dependencies {
    val jettyVersion = libs.versions.jetty.get()
    val slf4jVersion = libs.versions.slf4j.get()

    "api"(project(":http:http_client"))
    "api"("org.eclipse.jetty.http2:jetty-http2-client-transport:$jettyVersion")

    "testImplementation"("org.slf4j:log4j-over-slf4j:$slf4jVersion")
    "testImplementation"("org.slf4j:jcl-over-slf4j:$slf4jVersion")
    "testImplementation"("org.slf4j:slf4j-jdk14:$slf4jVersion")
}
