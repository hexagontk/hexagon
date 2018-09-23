
To use it you should add `apply from: $gradleScripts/bintray.gradle` to your `build.gradle` script
and `id 'com.jfrog.bintray' version 'VERSION'` to your `plugins` section in the root `build.gradle`.

To add variables to a build:

1. In `gradle.properties`
2. In `~/.gradle/gradle.properties`
3. Command line `-Pkey=val`
4. Inside `build.gradle`: `ext.key='val'`
