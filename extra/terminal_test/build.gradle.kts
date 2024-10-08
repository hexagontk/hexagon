
val gradleScripts = properties["gradleScripts"]

apply(from = "$gradleScripts/kotlin.gradle")
apply(from = "$gradleScripts/application.gradle")
apply(from = "$gradleScripts/native.gradle")
apply(from = "$gradleScripts/detekt.gradle")

description = "."

extensions.configure<JavaApplication> {
    mainClass.set("com.hexagonkt.application.test.ApplicationKt")
}

dependencies {
    "api"(project(":terminal"))
    "api"(project(":args"))
}
