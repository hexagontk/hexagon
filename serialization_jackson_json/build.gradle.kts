
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply(from = "../gradle/kotlin.gradle")
apply(from = "../gradle/publish.gradle")
apply(from = "../gradle/dokka.gradle")
apply(from = "../gradle/detekt.gradle")

description = "Hexagon CSV serialization format."

// IMPORTANT: Required for compiling classes in test dependencies. It *MUST* be before dependencies
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.dependsOn(tasks.getByPath(":core:compileTestKotlin"))
val coreTest: SourceSetOutput = project(":core").sourceSet("test").output

extra["basePackage"] = "com.hexagonkt.serialization"

dependencies {
    val kotlinVersion = properties["kotlinVersion"]
    val jacksonVersion = properties["jacksonVersion"]

    "api"(project(":serialization"))

    "api"("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    "api"("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    "api"("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    "api"("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
    "api"("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion") {
        exclude("org.jetbrains.kotlin")
    }

    "testImplementation"(coreTest)
}
