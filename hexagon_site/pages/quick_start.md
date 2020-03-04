
# Simple HTTP service

In this guide, we are going to create a sample HTTP service. You can read the [Core] or
[HTTP Server] modules documentation for more information.

You can start by cloning a starter project ([Gradle Starter] or [Maven Starter]). Or you can create
a project from scratch following these steps:

1. Configure [Kotlin] in [Gradle][Setup Gradle] or [Maven][Setup Maven].
2. Setup the [JCenter] and [Hexagon] repositories (follow the links and click on the `Set me up!`
   button).

```groovy tab="build.gradle"
repositories {
    maven { url  "https://dl.bintray.com/hexagonkt/hexagon" }
}
```

```xml tab="pom.xml"
<repositories>
  <repository>
    <id>hexagon</id>
    <url>https://dl.bintray.com/hexagonkt/hexagon</url>
  </repository>
</repositories>
```

3. Add the dependency in [Gradle] or [Maven]:

```groovy tab="build.gradle"
implementation("com.hexagonkt:http_server_jetty:$hexagonVersion")
```

```xml tab="pom.xml"
<dependency>
  <groupId>com.hexagonkt</groupId>
  <artifactId>http_server_jetty</artifactId>
  <version>$hexagonVersion</version>
</dependency>
```

4. Write the code in the `src/main/kotlin/Hello.kt` file:

@sample hexagon_starters/src/main/kotlin/com/hexagonkt/starters/Service.kt

5. Run the service and view the results at: [http://localhost:2010/hello/world][Endpoint]

# Next Steps

To continue learning about this toolkit, you can:

* Check the [Developer Guide] for more details.
* Clone the [Gradle Starter] or [Maven Starter] repository for a minimal fully working example
  (including tests).
* Proceed to the [Examples] section to check code snippets or full example projects.

[Gradle Starter]: https://github.com/hexagonkt/gradle_starter
[Maven Starter]: https://github.com/hexagonkt/maven_starter
[Examples]: /examples/http_server_examples/index.html
[Kotlin]: https://kotlinlang.org
[Setup Gradle]: https://kotlinlang.org/docs/reference/using-gradle.html
[Setup Maven]: https://kotlinlang.org/docs/reference/using-maven.html
[JCenter]: https://bintray.com/bintray/jcenter
[Hexagon]: https://bintray.com/hexagonkt/hexagon
[Gradle]: https://gradle.org
[Maven]: https://maven.apache.org
[Endpoint]: http://localhost:2010/hello/world
[Developer Guide]: /developer_guide/index.html
[Core]: /hexagon_core/index.html
[HTTP Server]: /port_http_server/index.html
