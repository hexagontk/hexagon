
Dokka
=====

This script setup Dokka tool and add the JAR with the documentation to the published JARs.

All modules Markdown files are added to the documentation and all test classes are available to be
referenced as samples.

To use it you should add `apply from: $gradleScripts/dokka.gradle` to your `build.gradle` script
and `id 'org.jetbrains.dokka' version 'VERSION'` to your `plugins` section in the root `build.gradle`.

This helper settings are:

* dokkaOutputFormat (optional): documentation format. By default it is 'gfm'.

For more reference, check the usage in `.travis.yaml` and `build.gradle`
