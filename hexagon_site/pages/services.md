
Services
========

## Create a service

### From scratch

To build Hexagon services you have some Gradle helpers that you can use on your own project. To
use them, you can use the online versions, or copy them to your `gradle` directory.

You can write a [Gradle] project from scratch (Gradle 3 or newer is required):

`build.gradle`:

```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.2.0'
}

apply plugin: "kotlin"
apply plugin: "application"

mainClassName = 'HelloKt'

repositories {
    jcenter ()
}

dependencies {
    compile ("com.hexagonkt:server_jetty:0.22.4")
}
```

Now you can run the service with `gradle run` and view the results at:
[http://localhost:2010/hello/world](http://localhost:2010/hello/world)

### From a template

You can create a service from a [Lazybones] template. To do so type:
`lazybones create hexagon-service service`

```bash
curl -s get.sdkman.io | bash && source ~/.sdkman/bin/sdkman-init.sh
sdk i lazybones
mkdir ~/.lazybones # Bug with Lazybones?
lazybones config set bintrayRepositories pledbrook/lazybones-templates jamming/maven
lazybones create hexagon-service service -Pgroup=org.example -Pversion=0.1 -Pdescription=Description
cd service
gradle/wrapper
```

## Directory structure

The project uses the standard Gradle structure. Gradle wrapper changed to be stored in the `gradle`
folder instead of the project root

## Running and Testing

gw run
gw -x test -t runService # Continuous mode AKA watch

## Packaging and Deployment

Build scripts:

  * `kotlin.gradle`: Sets up Kotlin's Gradle plugin: Adds Kotlin libraries, setup coverage report,
    filter project resources with build variables. To use it you need to:

    - Define the `kotlinVersion` variable to `gradle.properties` file.
    - Add the following code to `build.gradle`

```Groovy
      buildscript {
          repositories {
              jcenter ()
          }

          dependencies {
              classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
          }
      }
```

  * `service.gradle`: Gradle's script for a service or application.

    Tasks:
    - runService: for continuous run (AKA "Watch")
    - install: `systemdService`: script that support start/stop/status
    - buildInfo / processResources

    Variables:
    - deployDir
    - serviceUser
    - serviceGroup

    Use Systemd service:

    Copy this file to '/etc/systemd/system' and then:
      - To start the service execute: sudo systemctl start ${projectName}
      - To run the service at boot type: sudo systemctl enable ${projectName}

  * `site.gradle`: Adds support for site generation (with API documentation and reports).
    - siteSource
    - siteTarget

    To apply this script, you need to add the JBake plugin manually at the top of your build script
    as that is not possible in included scripts like this one. These are the required lines to do so:

    ```gradle
    plugins {
        id 'org.xbib.gradle.plugin.jbake' version '1.2.1'
    }
    ```

## Configuration files

* service.yaml (see configuration.md)
* logback.xml

Templates: Pebble (optional dependencies)

## Deploy on Servlet engine

## Deploy on Docker
