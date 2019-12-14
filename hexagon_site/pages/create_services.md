
# Cloning a Starter repository

To create a new Hexagon service, you can clone a minimum running example using the [Gradle Starter]
or the [Maven Starter].

# From scratch

You can write a [Gradle] project from scratch (Gradle 5.3 or newer is required) using the following
`build.gradle`:

```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.3.61'
}

apply plugin: 'kotlin'
apply plugin: 'application'

mainClassName = 'HelloKt'

repositories {
    jcenter ()
}

dependencies {
    compile ("com.hexagonkt:http_server_jetty:$hexagonVersion")
}
```

# From a template

You can create a service from a [Lazybones] template. To do so type:
`lazybones create hexagon-service srvName`

```bash
curl -s get.sdkman.io | bash && source ~/.sdkman/bin/sdkman-init.sh
sdk i lazybones
mkdir ~/.lazybones
lazybones config set bintrayRepositories pledbrook/lazybones-templates jamming/maven
lazybones create hexagon-service srvName -Pgroup=org.example -Pversion=0.1 -Pdescription=Description
cd srvName
./gradlew
```

[Gradle Starter]: https://github.com/hexagonkt/gradle_starter
[Maven Starter]: https://github.com/hexagonkt/maven_starter
[Gradle]: https://gradle.org
[Lazybones]: https://github.com/pledbrook/lazybones
