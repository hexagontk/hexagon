
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
    val nettyVersion = properties["nettyVersion"]
//    val nettyTcNativeVersion = properties["nettyTcNativeVersion"]
    val slf4jVersion = properties["slf4jVersion"]

    "api"(project(":http:http_client"))
    "api"("io.netty:netty-codec-http2:$nettyVersion") { exclude(group = "org.slf4j") }

//    if (System.getProperty("os.name").lowercase().contains("mac"))
//        "api"("io.netty:netty-tcnative:$nettyTcNativeVersion:osx-x86_64") {
//            exclude(group = "org.slf4j")
//        }

    "testImplementation"(project(":http:http_test"))
    "testImplementation"(project(":http:http_server_jetty"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))
    "testImplementation"(project(":serialization:serialization_jackson_yaml"))
    "testImplementation"("org.slf4j:log4j-over-slf4j:$slf4jVersion")
    "testImplementation"("org.slf4j:jcl-over-slf4j:$slf4jVersion")
    "testImplementation"("org.slf4j:slf4j-jdk14:$slf4jVersion")
}
