
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/bintray.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/testng.gradle")

dependencies {
    "api"(project(":port_templates"))
    "api"("io.pebbletemplates:pebble:${properties.get("pebbleVersion")}") { exclude module = "slf4j-api" }
}
