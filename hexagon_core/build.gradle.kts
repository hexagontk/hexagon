
apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/bintray.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/testng.gradle")

dependencies {
    val slf4jVersion = properties["slf4jVersion"]
    val logbackVersion = properties["logbackVersion"]
    val jacksonVersion = properties["jacksonVersion"]

    "api"("org.slf4j:slf4j-api:$slf4jVersion")
    "api"("org.slf4j:jcl-over-slf4j:$slf4jVersion") { exclude(module = "slf4j-api") }
    "api"("org.slf4j:jul-to-slf4j:$slf4jVersion") { exclude(module = "slf4j-api") }
    "api"("ch.qos.logback:logback-classic:$logbackVersion") { exclude(module = "slf4j-api") }

    "api"("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    "api"("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:$jacksonVersion")
    "api"("com.fasterxml.jackson.dataformat:jackson-dataformat-properties:$jacksonVersion")
    "api"("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    "api"("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    "api"("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
    "api"("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion") {
        exclude(group = "org.jetbrains.kotlin")
    }
}
