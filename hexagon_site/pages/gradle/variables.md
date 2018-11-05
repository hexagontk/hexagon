
Build Variables
===============

It is possible to add/change variables of a build from the following places:

1. In the project's `gradle.properties` file.
2. In your user's gradle configuration: `~/.gradle/gradle.properties`.
3. Passing them from the command line with the following switch: `-Pkey=val`.
4. Defining a project's extra property inside `build.gradle`. Ie: `project.ext.key='val'`.

For examples and reference, check [.travis.yml], [build.gradle] and [gradle.properties].

[.travis.yml]: https://github.com/hexagonkt/hexagon/blob/master/.travis.yml
[build.gradle]: https://github.com/hexagonkt/hexagon/blob/master/build.gradle
[gradle.properties]: https://github.com/hexagonkt/hexagon/blob/master/gradle.properties
