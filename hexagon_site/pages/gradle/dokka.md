
Dokka
=====

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
