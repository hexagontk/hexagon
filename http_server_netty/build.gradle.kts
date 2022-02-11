
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

dependencies {
    val nettyVersion = properties["nettyVersion"]

    "api"(project(":http_server"))
    "api"("io.netty:netty-codec-http:$nettyVersion")
    "api"("io.netty:netty-codec-http2:$nettyVersion")

    "testImplementation"(project(":http_test"))
    "testImplementation"(project(":http_client_jetty"))
}
