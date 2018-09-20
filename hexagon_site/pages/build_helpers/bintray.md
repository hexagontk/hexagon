
Bintray
=======

This script setup the project/module for publishing in [Bintray].

It publishes all artifacts attached to the `mavenJava` publication (check [kotlin.gradle] publishing
section) at the bare minimum binaries are published. For an Open Source project, you must include
sources and javadocs also.

To use it you should add `apply from: $gradleScripts/bintray.gradle` to your `build.gradle` script
and `id 'com.jfrog.bintray' version 'VERSION'` to your `plugins` section in the root `build.gradle`.

This helper settings are:

* bintrayKey : if not defined will try to load BINTRAY_KEY environment variable.
* bintrayUser : or BINTRAY_USER environment variable if not defined.
* bintrayRepo :
* license :
* vcsUrl :

To add variables to a build:

1. In `gradle.properties`
2. In `~/.gradle/gradle.properties`
3. Command line `-Pkey=val`
4. Inside `build.gradle`: `ext.key='val'`

For more reference, check the usage in `.travis.yaml` and `hexagon_*/build.gradle`
