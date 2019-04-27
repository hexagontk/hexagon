
# Build Variables

It is possible to add/change variables of a build from the following places:

1. In the project's `gradle.properties` file.
2. In your user's gradle configuration: `~/.gradle/gradle.properties`.
3. Passing them from the command line with the following switch: `-Pkey=val`.
4. Defining a project's extra property inside `build.gradle`. Ie: `project.ext.key='val'`.

For examples and reference, check [.travis.yml], [build.gradle] and [gradle.properties].

[.travis.yml]: https://github.com/hexagonkt/hexagon/blob/master/.travis.yml
[build.gradle]: https://github.com/hexagonkt/hexagon/blob/master/build.gradle
[gradle.properties]: https://github.com/hexagonkt/hexagon/blob/master/gradle.properties

# Helper scripts

These scripts can be added to your build to include a whole new capability to your building logic.

To use them, you can import the online versions, or copy them to your `gradle` directory before
importing the script.

## Bintray

This script setup the project/module for publishing in [Bintray].

It publishes all artifacts attached to the `mavenJava` publication (check [kotlin.gradle] publishing
section) at the bare minimum binaries are published. For an Open Source project, you must include
sources and javadoc also.

To use it you should add `apply from: $gradleScripts/bintray.gradle` to your `build.gradle` script
and `id 'com.jfrog.bintray' version 'VERSION'` to your `plugins` section in the root `build.gradle`.

To setup this script's parameters, check the [build variables section]. This helper settings are:

* bintrayKey (REQUIRED): if not defined will try to load BINTRAY_KEY environment variable.
* bintrayUser (REQUIRED): or BINTRAY_USER environment variable if not defined.
* bintrayRepo (REQUIRED): Bintray's repository to upload the artifacts.
* license (REQUIRED): the license used to publish in Bintray.
* vcsUrl (REQUIRED): code repository location.

[Bintray]: https://bintray.com
[kotlin.gradle]: https://github.com/hexagonkt/hexagon/blob/master/gradle/kotlin.gradle
[build variables section]: /gradle/variables.html

## Dokka

This script setup [Dokka] tool and add a JAR with the project's code documentation to the published
JARs.

All modules' Markdown files are added to the documentation and all test classes are available to be
referenced as samples.

To use it you should add `apply from: $gradleScripts/dokka.gradle` to your `build.gradle` script
and `id 'org.jetbrains.dokka' version 'VERSION'` to your `plugins` section in the root
`build.gradle`.

To setup this script's parameters, check the [build variables section]. This helper settings are:

* dokkaOutputFormat (optional): documentation format. By default it is `gfm`.

[Dokka]: https://github.com/Kotlin/dokka
[build variables section]: /gradle/variables.html

## Icons

Create web icons (favicon and thumbnails for browsers/mobile) from image SVGs (logos).

For image rendering you will need [rsvg] (librsvg2-bin) and [imagemagick] installed in the
development machine.

To use it you should add `apply from: $gradleScripts/icons.gradle` to your `build.gradle`.

To setup this script's parameters, check the [build variables section]. This helper settings are:

* logoSmall (REQUIRED): SVG file used to render the small logo. Used for the favicon.
* logoLarge (REQUIRED): SVG file used to render the large logo.
* logoWide (REQUIRED): SVG file used to render the wide logo. Used for MS Windows tiles.

[rsvg]: https://github.com/GNOME/librsvg
[imagemagick]: https://www.imagemagick.org
[build variables section]: /gradle.html#build-variables

## JMH

To use it you should add `apply from: $gradleScripts/jmh.gradle` to your `build.gradle` script
and `id 'me.champeau.gradle.jmh' version 'VERSION'` to your `plugins` section in the root
`build.gradle`.

Sample benchmark:

```kotlin
import org.openjdk.jmh.annotations.Benchmark

open class Benchmark {
    @Benchmark fun foo() {
        println("foo bench")
        Thread.sleep(100L)
    }

    @Benchmark fun bar() {
        println("bar bench")
        Thread.sleep(100L)
    }
}
```

## Kotlin

Sets up Kotlin's Gradle plugin: Adds Kotlin libraries, setup coverage report, filter project
resources with build variables. To use it you need to:

- Define the `kotlinVersion` variable to `gradle.properties` file.
- Define the `kotlinCoroutinesVersion` variable to `gradle.properties` file.

Helps with:

* Setting Java version
* Repositories
* Kotlin dependencies
* Resource processing
* Tests (ITs, unit, pass properties, output)
* Published artifacts (binaries, sources and test)
* Jar with dependencies

## Kotlin JS

This script must be applied at the end of the build script.
 
Applying this script at the beginning won't work until it allows dependencies to be merged (a bug).

## Service

Gradle's script for a service or application.

Extra tasks:

* buildInfo : add configuration file with build variables to the package
* runService : Run the service in another thread. This allow the possibility to 'watch' source
  changes. To run the services and watch for changes you need to execute this task with the
  `--continuous` (`-t`) Gradle flag. Ie: `gw -t runService`

## JBake

To generate the site execute: `gw bake` and to test it run: `gw bakePreview`.

The site will be served at: [http://localhost:8888](http://localhost:8888). You can change the port
defining the `sitePort` variable inside `gradle.properties`.

Adds support for site generation (with API documentation and reports).

To apply this script, you need to add the JBake plugin manually at the top of your build script
as that is not possible in included scripts like this one. These are the required lines to do so:

```gradle
plugins {
    id "org.jbake.site" version "1.4.0"
}
```

Generate service documentation site.

JBake `content` folder can not be changed (it seems a bug).

To generate clean URLs, add the following settings:

```groovy
configuration['uri.noExtension'] = true
configuration['uri.noExtension.prefix'] = '/'
```

## SonarQube

Set up the project to be analyzed by the [SonarQube instance running in the cloud][sonarcloud].

The available configuration parameters are:

* sonarqubeProject (REQUIRED):
* sonarqubeOrganization (REQUIRED):
* sonarqubeHost: SonarQube server to be used. By default it is: `https://sonarcloud.io`.
* sonarqubeToken (REQUIRED): If not set, the `SONARQUBE_TOKEN` environment variable will be used.

[sonarcloud]: https://sonarcloud.io

## TestNG

Uses TestNG as the test framework.
