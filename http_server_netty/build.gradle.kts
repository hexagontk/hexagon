
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

extra["basePackage"] = "com.hexagonkt.http.server.netty"

dependencies {
    val nettyVersion = properties["nettyVersion"]

    "api"(project(":http_server"))
    "api"("io.netty:netty-all:$nettyVersion")

    "testImplementation"(project(":http_test"))
    "testImplementation"(project(":http_client_jetty"))
}
