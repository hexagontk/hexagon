import gg.jte.ContentType.Html

plugins {
    id("java-library")
    id("gg.jte.gradle") version("3.1.3")
}

apply(from = "$rootDir/gradle/kotlin.gradle")
apply(from = "$rootDir/gradle/publish.gradle")
apply(from = "$rootDir/gradle/dokka.gradle")
apply(from = "$rootDir/gradle/native.gradle")
apply(from = "$rootDir/gradle/detekt.gradle")

description = "Template processor adapter for 'jte'."

dependencies {
    val jteVersion = properties["jteVersion"]

    "api"(project(":templates:templates"))
    "api"("gg.jte:jte:$jteVersion")

    "testImplementation"(project(":templates:templates_test"))
    "testImplementation"(project(":serialization:serialization_jackson_json"))

    "jteGenerate"("gg.jte:jte-native-resources:$jteVersion")
}

tasks.named("compileKotlin") { dependsOn("generateJte") }
tasks.named("processResources") { dependsOn("processTestResources") }
tasks.named("detektMain") { dependsOn("compileTestKotlin") }

// TODO Remove when settings prevent this directory from being created (check .gitignore also)
tasks.named<Delete>("clean") {
    delete("jte-classes")
}

jte {
    sourceDirectory.set(projectDir.resolve("src/test/resources/templates").toPath())
    targetDirectory.set(projectDir.resolve("build/classes/kotlin/test").toPath())
    contentType.set(Html)

    jteExtension("gg.jte.nativeimage.NativeResourcesExtension")

    generate()
}
