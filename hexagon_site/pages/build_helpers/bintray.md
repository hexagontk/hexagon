
Bintray
=======

This script setup the project/module for publishing in [Bintray].

It publishes all artifacts attached to the `mavenJava` publication (check [kotlin.gradle] publishing
section) at the bare minimum binaries are published. For an Open Source project, you must include
sources and javadocs also.

To use it you should add `apply from: $gradleScripts/bintray.gradle` to your `build.gradle` script
and `id 'com.jfrog.bintray' version 'VERSION'` to your `plugins` section in the root `build.gradle`.

This helper settings are:

* bintrayKey (REQUIRED): if not defined will try to load BINTRAY_KEY environment variable.
* bintrayUser (REQUIRED): or BINTRAY_USER environment variable if not defined.
* bintrayRepo (REQUIRED): Bintray's repository to upload the artifacts.
* license (REQUIRED): the license used to publish into Bintray.
* vcsUrl (REQUIRED): code repository location.

For more reference, check the usage in `.travis.yaml` and `build.gradle`
