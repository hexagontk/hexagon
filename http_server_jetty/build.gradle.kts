
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

    "api"(project(":http_server_servlet"))
    "api"(platform("org.eclipse.jetty:jetty-bom:$jettyVersion"))
    "api"("org.eclipse.jetty:jetty-webapp")
    "api"("org.eclipse.jetty.http2:http2-server")
    "api"("org.eclipse.jetty:jetty-alpn-java-server")

    "testImplementation"(project(":http_client_jetty_ws"))
    "testImplementation"("org.eclipse.jetty.websocket:websocket-jetty-server")
}
