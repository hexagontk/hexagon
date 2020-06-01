
# Build Variables

The build process and imported build scripts (like the ones documented here) use variables to
customize their behaviour. It is possible to add/change variables of a build from the following
places:

1. In the project's `gradle.properties` file.
2. In your user's gradle configuration: `~/.gradle/gradle.properties`.
3. Passing them from the command line with the following switch: `-Pkey=val`.
4. Defining a project's extra property inside `build.gradle`. Ie: `project.ext.key='val'`.

For examples and reference, check [build.gradle] and [gradle.properties].

[build.gradle]: https://github.com/hexagonkt/hexagon/blob/master/build.gradle
[gradle.properties]: https://github.com/hexagonkt/hexagon/blob/master/gradle.properties

# Helper scripts

These scripts can be added to your build to include a whole new capability to your building logic.

To use them, you can import the online versions, or copy them to your `gradle` directory before
importing the script.

You can import these scripts by adding add `apply from: $gradleScripts/$script.gradle` to your
`build.gradle` file some of them may require additional plugins inside the `plugins` section in the
root `build.gradle`. Check toolkit's `build.gradle` files for examples.

## Bintray

This script set up the project/module for publishing in [Bintray].

It publishes all artifacts attached to the `mavenJava` publication (check [kotlin.gradle] publishing
section) at the bare minimum binaries are published. For an Open Source project, you must include
sources and javadoc also.

To use it apply `$gradleScripts/bintray.gradle` and add the
`id 'com.jfrog.bintray' version 'VERSION'` plugin to the root `build.gradle`.

To set up this script's parameters, check the [build variables section]. This helper settings are:

* bintrayKey (REQUIRED): if not defined will try to load BINTRAY_KEY environment variable.
* bintrayUser (REQUIRED): or BINTRAY_USER environment variable if not defined.
* bintrayRepo (REQUIRED): Bintray's repository to upload the artifacts.
* license (REQUIRED): the license used to publish in Bintray.
* vcsUrl (REQUIRED): code repository location.
* bintrayPublications: list of Maven publications published . By default `[ "mavenJava" ]`.
* bintrayDryRun: when set to true, no actual publishing happens. Default value is false.

[Bintray]: https://bintray.com
[kotlin.gradle]: https://github.com/hexagonkt/hexagon/blob/master/gradle/kotlin.gradle
[build variables section]: /gradle/#build-variables

## Dokka

This script set up [Dokka] tool and add a JAR with the project's code documentation to the published
JARs.

All modules' Markdown files are added to the documentation and test classes ending in `SamplesTest`
are available to be referenced as samples.

To use it apply `$gradleScripts/dokka.gradle` and add the
`id 'org.jetbrains.dokka' version 'VERSION'` plugin to the root `build.gradle`.

The format for the generated documentation will be `javadoc` to make it compatible with current
IDEs.

[Dokka]: https://github.com/Kotlin/dokka

## Icons

Create web icons (favicon and thumbnails for browsers/mobile) from image SVGs (logos).

For image rendering you will need [rsvg] (librsvg2-bin) and [imagemagick] installed in the
development machine.

To use it, apply `$gradleScripts/icons.gradle` to your `build.gradle`.

To set up this script's parameters, check the [build variables section]. This helper settings are:

* logoSmall (REQUIRED): SVG file used to render the small logo. Used for the favicon.
* logoLarge (REQUIRED): SVG file used to render the large logo.
* logoWide (REQUIRED): SVG file used to render the wide logo. Used for MS Windows tiles.

[rsvg]: https://github.com/GNOME/librsvg
[imagemagick]: https://www.imagemagick.org

## Kotlin

Adds Kotlin's Gradle plugin.

Uses [JUnit 5] as the test framework. It also includes [Kotest] in the test classpath.

It sets up:

- Java version
- Repositories
- Kotlin dependencies
- Resource processing (replacing build variables)
- Cleaning (deleting runtime files as logs and dump files)
- Tests (pass properties, output and mocks). Test's output depends on Gradle logging level
- Set up coverage report
- IDE settings for IntelliJ and Eclipse (download dependencies' sources and API documentation)
- Published artifacts (binaries, sources and test): sourceJar and testJar tasks
- Jar with dependencies: jarAll task

To use it, apply `$gradleScripts/kotlin.gradle` and add the
`id 'org.jetbrains.kotlin.jvm' version 'VERSION'` plugin to the root `build.gradle`.

To set up this script's parameters, check the [build variables section]. This helper settings are:

* kotlinVersion: Kotlin version. Defaults to the version used in the matching Hexagon release.
* mockkVersion: MockK mocking library version. If no value is supplied, Hexagon's version is taken.
* junitVersion: JUnit version (5+), the default value is the toolkit's version.
* kotestVersion: Kotest version, the default value is the version used by Hexagon.

[JUnit 5]: https://junit.org
[Kotest]: https://github.com/kotest/kotest

## Kotlin JS

This script provides the following tasks for compiling Kotlin to JavaScript:

* `jsAll`: compiles the project to JavaScript including all of
  its dependencies. It copies all the resulting files to `build/js`.
* `assembleWeb`: copies all project resources and JavaScript files to `build/web`.

IMPORTANT: This script must be applied at the end of the build script.

To use it apply `$gradleScripts/kotlin_js.gradle` at the end of the build script, also apply the
`kotlin2js` plugin. And finally, add the `id 'org.jetbrains.kotlin.jvm' version 'VERSION'` plugin to
the root `build.gradle`.

Applying this script at the beginning won't work until it allows dependencies to be merged (a bug).

To set up this script's parameters, check the [build variables section]. This helper settings are:

* javaScriptDirectory: JavaScript directory inside the `web` directory. By default, it is: "js".

## Application

Gradle's script for a service or application. It adds two extra tasks:

* buildInfo: add configuration file (`application.properties`) with build variables to the package.
  It is executed automatically before compiling classes.
* watch: Run the application in another thread. This allows the possibility to watch source changes.
  To run the application and watch for changes you need to execute this task with the `--continuous`
  (`-t`) Gradle flag. Ie: `gw -t watch`.
* jarAll: creates a single JAR with all dependencies, and the application main class set up. This
  task is an alternative to the Gradle `installDist` task.

To use it, apply `$gradleScripts/application.gradle` to your `build.gradle`.

To set up this script you need to add the main class name to your `build.gradle` file with the
following code:

```groovy
application {
    mainClassName = "com.example.ApplicationKt"
}
```

## Certificates

Creates the required key stores for development purposes. **IMPORTANT** these key stores must not be
used for production environments.

The created key stores are:

* `ca.p12`: self signed certificate authority (CA). This store holds the CA private key. The store
  must be private and will be used to sign other certificates. The key pair alias is `ca`.
* `trust.p12`: key store with CA's public certificate. It can be set as the Java process trust store
  which make every certificate signed with the CA trusted. However, if used as the trust store, the
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

To use it, apply `$gradleScripts/certificates.gradle` to your `build.gradle`.

To set up this script's parameters, check the [build variables section]. This helper settings are:

* sslDomain\[1-9] (REQUIRED): the main domain for the identity store. You can create up to ten (from
 `sslDomain` to `sslDomain9`). Each of these variables has the format
 `subdomain1|subdomain2|subdomainN|domain.tld` subdomains are added to `<domain>.p12` alternative
 names (aside of `<domain>.test` and `localhost` which are always added). By default, no extra
 domains are added to the key store.
* sslOrganization (REQUIRED): organization stated in created certificates.
* sslCaFile: certificate authority key store file. By default: "ca.p12".
* sslCaAlias: CA alias in the key store. If not provided, it will be "ca".
* sslTrustFile: trust store file name, by default it is "trust.p12".
* sslPath: path used to generate the key stores. By default, it will be project's build directory.
* sslPassword: password used for the generated key stores. By default, it is the file name reversed.
* sslValidity: validity period (in days) for certificates. If not provided, it will be 365.
* sslCountry: country used in the certificates. By default, it is the current locale's country code.

[TLD for local environments]: https://tools.ietf.org/html/rfc2606

## Lean

This script changes the default Gradle source layout to be less bulky. To use it you must apply the
`$gradleScripts/lean.gradle` script to your `build.gradle` file. It must be applied after the
Kotlin plugin.

After applying this script, the source folders will be `${projectDir}/main` and
`${projectDir}/test`, and the resources will be stored also in these folders.
