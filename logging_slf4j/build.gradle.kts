
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

description = "Hexagon SLF4J logging adapter."

extra["basePackage"] = "com.hexagonkt.logging"

dependencies {
    val slf4jVersion = properties["slf4jVersion"]
    val logbackVersion = properties["logbackVersion"]

    "api"(project(":hexagon_core"))
    "api"("org.slf4j:slf4j-api:$slf4jVersion")

    "testImplementation"("ch.qos.logback:logback-classic:$logbackVersion") { exclude("org.slf4j") }
}
