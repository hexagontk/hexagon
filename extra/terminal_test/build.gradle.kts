
apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/application.gradle")

if (findProperty("fullBuild") != null) {
    apply(from = "$rootDir/gradle/native.gradle")
    apply(from = "$rootDir/gradle/detekt.gradle")
}

description = "."

extensions.configure<JavaApplication> {
    mainClass.set("com.hexagontk.application.test.ApplicationKt")
}

dependencies {
    "api"(project(":extra:terminal"))
    "api"(project(":extra:args"))
}
