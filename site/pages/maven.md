
# Parent POMs
If you want to use [Maven] to build your Kotlin applications, you can set up your POM to inherit
from the Hexagon's parent POM.

This POM configures Kotlin and Hexagon for you. There are two different flavors available (based
on the directory schema you want to use):

* The [standard layout POM]
* The [lean layout POM]

## Standard Parent POM
This layout is the well-known standard one, it has more directories but its widely used. These are
the features it provides:

* Set up the Kotlin plugin
* Use [JUnit 5] and [Kotlin Test] for testing
* Configure [Jacoco] coverage report

```xml
<parent>
  <groupId>com.hexagonkt</groupId>
  <artifactId>kotlin_pom</artifactId>
  <version>$hexagonVersion</version>
</parent>
```

## Lean Parent POM
This directory layout has less nested directories, and it is more compact. The main downside of
using this approach is that it differs of the standard one.

* Inherits from the Standard Parent POM (it provides all its features)
* Change the source directories to be `main` and `test` instead `src/{main,test}/kotlin`
* Store resources together with source files instead of `src/<sourceSet>/resources`

```xml
<parent>
  <groupId>com.hexagonkt</groupId>
  <artifactId>kotlin_lean_pom</artifactId>
  <version>$hexagonVersion</version>
</parent>
```

[Maven]: https://maven.apache.org

[standard layout POM]: https://search.maven.org/search?q=a:kotlin_pom
[lean layout POM]: https://search.maven.org/search?q=a:kotlin_lean_pom

[JUnit 5]: https://junit.org/junit5
[Kotlin Test]: https://kotlinlang.org/api/latest/kotlin.test/
[MockK]: https://mockk.io
[Jacoco]: https://www.eclemma.org/jacoco
