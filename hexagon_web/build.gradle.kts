
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")

extra["basePackage"] = "com.hexagonkt.web"

repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

dependencies {
    "api"(project(":port_http_server"))
    "api"(project(":port_templates"))

    "api"("org.jetbrains.kotlinx:kotlinx-html-jvm:${properties["kotlinxHtmlVersion"]}")

    "testImplementation"(project(":http_client_ahc"))
    "testImplementation"(project(":http_server_jetty"))
    "testImplementation"(project(":templates_pebble"))
    "testImplementation"(project(":serialization_yaml"))
}
