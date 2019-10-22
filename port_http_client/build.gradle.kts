
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/bintray.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/testng.gradle")

dependencies {
    "compile"(project(":hexagon_core"))
    "compile"("org.asynchttpclient:async-http-client:${properties.get("ahcVersion")}"){
        exclude(module = "slf4j-api")
    }

    "testCompile"(project(":http_server_jetty"))
}
