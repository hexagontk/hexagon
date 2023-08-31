
# Build Variables
The build process and imported build scripts (like the ones documented here) use variables to
customize their behavior. It is possible to add/change variables of a build from the following
places:

1. Passing them from the command line with the following switch: `-P key=val`.
2. In the project's `gradle.properties` file.
3. Defining a project's extra property inside `build.gradle.kts`. Ie: `project.ext["key"] = "val"`.
4. In your user's gradle configuration: `~/.gradle/gradle.properties`.

For examples and reference, check [build.gradle.kts] and [gradle.properties].

[build.gradle.kts]: https://github.com/hexagonkt/hexagon/blob/master/build.gradle.kts
[gradle.properties]: https://github.com/hexagonkt/hexagon/blob/master/gradle.properties

# Helper scripts
These scripts can be added to your build to include a whole new capability to your building logic.

To use them, you can import the online versions, or copy them to your `gradle` directory before
importing the script.

You can import these scripts by adding `apply(from = "$gradleScripts/$script.gradle")` to your
`build.gradle.kts`, some of them may require additional plugins inside the `plugins` section in the
root `build.gradle.kts`. Check toolkit `build.gradle.kts` files for examples.

## Publish
This script set up the project/module for publishing in [Maven Central].

It publishes all artifacts attached to the `mavenJava` publication (check [kotlin.gradle] publishing
section).

The binaries are always published. For an Open Source project, you must also include sources and
javadoc.

To use it, apply `$gradleScripts/publish.gradle`.

To set up this script's parameters, check the [build variables section]. These helper settings are:

* signingKey (REQUIRED): if not defined will try to load SIGNING_KEY environment variable.
* signingPassword (REQUIRED): or SIGNING_PASSWORD environment variable if not defined.
* ossrhUsername (REQUIRED): or OSSRH_USERNAME if not found.
* ossrhPassword (REQUIRED): or OSSRH_PASSWORD if not found.
* licenses (REQUIRED): the licenses used in published POMs.
* siteHost (REQUIRED): project's website.

[Maven Central]: https://search.maven.org
[kotlin.gradle]: https://github.com/hexagonkt/hexagon/blob/master/gradle/kotlin.gradle
[build variables section]: /gradle/#build-variables

## Dokka
This script set up [Dokka] tool and add a JAR with the project's code documentation to the published
JARs. It adds the following extra task:

* dokkaJar: create a jar file with the source code documentation in Javadoc format.

All modules' Markdown files are added to the documentation and test classes ending in `SamplesTest`
are available to be referenced as samples.

To use it, apply `$gradleScripts/dokka.gradle` and add the
`id("org.jetbrains.dokka") version("VERSION")` plugin to the root `build.gradle.kts`.

The format for the generated documentation will be `javadoc` to make it compatible with current
IDEs.

[Dokka]: https://github.com/Kotlin/dokka

## Icons
Create web icons (favicon and thumbnails for browsers/mobile) from SVG images (logos).

For image rendering you will need [rsvg] (librsvg2-bin) and [imagemagick] installed in the
development machine.

To use it, apply `$gradleScripts/icons.gradle` to your `build.gradle.kts`.

To set up this script's parameters, check the [build variables section]. These helper settings are:

* logo (REQUIRED): SVG file used to render the logos. Used for the favicon.
* logoSmall: SVG file used to render the small logo. If not supplied 'logo' will be used.
* logoLarge: SVG file used to render the large logo. If not supplied 'logoSmall' will be used.
* iconsDirectory: directory inside `build` where icons will be generated. The default is `icons`.

[rsvg]: https://github.com/GNOME/librsvg
[imagemagick]: https://imagemagick.org/index.php

## Kotlin
Adds Kotlin's Gradle plugin.

Uses [JUnit 5] as the test framework. It also includes [MockK] in the test classpath.

It sets up:

- Java version
- Repositories
- Kotlin dependencies
- Resource processing (replacing build variables)
- Cleaning (deleting runtime files as logs and dump files)
- Tests run, handles properties, output, and mocks (test's output depends on Gradle logging level)
- Set up coverage report
- IDE settings for IntelliJ and Eclipse (download dependencies' sources and API documentation)
- Published artifacts (binaries and sources): sourcesJar task

To use it, apply `$gradleScripts/kotlin.gradle` and add the
`id("org.jetbrains.kotlin.jvm") version("VERSION")` plugin to the root `build.gradle.kts`.

To set up this script's parameters, check the [build variables section]. These helper settings are:

* kotlinVersion: Kotlin version. Defaults to the version used in the matching Hexagon release.
* mockkVersion: MockK mocking library version. If no value is supplied, Hexagon's version is taken.
* junitVersion: JUnit version (5+), the default value is the toolkit version.
* basePackage: module's base package (used by the Jacoco Report when using Kotlin Coding Standard).
* jvmTarget: target JVM version (defines the bytecode version used). If not set, '11' will be used.

[JUnit 5]: https://junit.org
[MockK]: https://mockk.io

## Application
Gradle's script for a service or application. It adds these extra tasks:

* buildInfo: add configuration file (`META-INF/build.properties`) with build variables to the
  package. It is executed automatically before compiling classes.
* jarAll: creates a single JAR with all dependencies, and the application main class set up. This
  task is an alternative to the Gradle `installDist` task.
* jlink: create an application distribution based on a jlink generated JRE.
* jpackage: create a jpackage distribution including a JVM with a subset of the modules.
* tarJlink: compress Jpackage distribution in a single file.

To use it, apply `$gradleScripts/application.gradle` to your `build.gradle.kts`.

To set up this script's parameters, check the [build variables section]. These helper settings are:

* modules: comma separated list of modules to include in the generated JRE.
* options: JVM options passed to the jpackage generated launcher.
* icon: icon to be used in the jpackage distribution.
* applicationClass (REQUIRED): fully qualified name of the main class of the application.

To set up this script you need to add the main class name to your `build.gradle.kts` file with the
following code:

```groovy
application {
    mainClass.set("com.example.ApplicationKt")
}
```

## Certificates
Creates the required key stores for development purposes. **IMPORTANT** these key stores must not be
used for production environments.

The created key stores are:

* `ca.p12`: self-signed certificate authority (CA). This store holds the CA private key. The store
  must be private and will be used to sign other certificates. The key pair alias is `ca`.
* `trust.p12`: key store with CA's public certificate. It can be set as the Java process trust store
  which makes every certificate signed with the CA trusted. However, if used as the trust store, the
  JDK `cacerts` entries won't be loaded and thus, not trusted. It can be used to set up HTTPS
  clients (not required to be set at JVM level).
* `<domain>.p12`: there would be one per each domain (see `sslDomain` variable). These stores are
  signed by the CA, and they contain the service private key and its full chain certificate.
  `<domain>` will be the domain name without the TLD, and the Subject alternative names (SAN) will
  include `<domain>.test` ([TLD for local environments]) and `localhost` (along the extra subdomains
  specified).

The defined tasks are:

* createCa: creates `ca.p12` and import its public certificate inside `trust.p12`.
* createIdentities: creates the `<domain>.p12` store for all `sslDomain` variables.

To use it, apply `$gradleScripts/certificates.gradle` to your `build.gradle.kts`.

To set up this script's parameters, check the [build variables section]. These helper settings are:

* sslDomain\[1-9] (REQUIRED): the main domain for the identity store. You can create up to ten (from
 `sslDomain` to `sslDomain9`). Each of these variables has the format
 `subdomain1|subdomain2|subdomainN|domain.tld` subdomains are added to `<domain>.p12` alternative
 names (aside of `<domain>.test` and `localhost` which are always added). By default, no extra
 domains are added to the key store.
* sslOrganization (REQUIRED): organization stated in created certificates.
* sslCaFile: certificate authority key store file. By default: "ca.p12".
* sslCaAlias: CA alias in the key store. If not provided, it will be "ca".
* sslTrustFile: trust store file name, by default it is "trust.p12".
* sslPath: path used to generate the key stores. By default, it will be the project's build
  directory.
* sslPassword: password used for the generated key stores. By default, it is the file name reversed.
* sslValidity: validity period (in days) for certificates. If not provided, it will be 365.
* sslCountry: country used in the certificates. By default, it is the current locale's country code.

[TLD for local environments]: https://tools.ietf.org/html/rfc2606

## Lean
This script changes the default Gradle source layout to be less bulky. To use it you must apply the
`$gradleScripts/lean.gradle` script to your `build.gradle.kts` file. It must be applied after the
Kotlin plugin.

After applying this script, the source folders will be `${projectDir}/main` and
`${projectDir}/test`, and the resources will be stored also in these folders.

## Detekt
This script sets up the build to analyze the code with the [Detekt] static code analyzer. To use it
you must apply the `$gradleScripts/detekt.gradle` script to your `build.gradle.kts` file. It must be
applied after the Kotlin plugin.

For the script to work you need to add the plugin to the plugins build section before importing the
script. I.e.:

```kotlin
plugins {
    id("io.gitlab.arturbosch.detekt") version("VERSION") apply(false)
}
```

To set up this script's parameters, check the [build variables section]. These helper settings are:

* detektConfigPath: file with Detekt rules and settings. If not set, the default Detekt setup will
  be used.

[Detekt]: https://detekt.github.io/detekt

## Native
This script sets up the build to add [GraalVM] configuration files for [native image] generation
into JAR files (for library projects), and also allows to easily generate a native image for an
application.

The defined tasks are:

* upx: compress the native executable using 'upx'. NOTE: Makes binaries use more RAM!!!
* tarNative: compress native executable into a TAR file.

To use it you must apply the `$gradleScripts/native.gradle` script to your `build.gradle.kts` file.
It must be applied after the Kotlin plugin.

For the script to work you need to add the plugin to the plugins build section before importing the
script. I.e.:

```kotlin
plugins {
    id("org.graalvm.buildtools.native") version("VERSION")
}
```

If you want to create a native image for your application you should execute:

```bash
./gradlew nativeCompile
```

To set up this script's parameters, check the [build variables section]. These helper settings are:

[GraalVM]: https://www.graalvm.org
[native image]: https://graalvm.github.io/native-build-tools/latest/index.html

## JMH
Sets up the build to run JMH benchmarks. To use it you must apply the `$gradleScripts/jmh.gradle`
script to your `build.gradle.kts` file.

For the script to work you need to add the plugin to the plugins build section before importing the
script. I.e.:

```kotlin
plugins {
    id("me.champeau.jmh") version("VERSION") apply(false)
}
```

To set up this script's parameters, check the [build variables section]. These helper settings are:

* jmhVersion: JMH version to be used. If not specified a tested JMH version will be used.
