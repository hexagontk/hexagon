
plugins {
    id("java-library")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

dependencies {
    val jettyVersion = properties["jettyVersion"]

    "api"(project(":http:http_server_servlet"))
    "api"(platform("org.eclipse.jetty:jetty-bom:$jettyVersion"))
    "api"("org.eclipse.jetty.ee10:jetty-ee10-servlet:$jettyVersion")
    "api"("org.eclipse.jetty.http2:jetty-http2-server")
    "api"("org.eclipse.jetty:jetty-alpn-java-server")

    "testImplementation"(project(":http:http_client_jetty_ws"))
    "testImplementation"(
        "org.eclipse.jetty.ee10.websocket:jetty-ee10-websocket-jakarta-server:$jettyVersion"
    )
}
