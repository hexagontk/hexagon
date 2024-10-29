
In this guide, we are going to create a sample HTTP service. You can read the [Core] or
[HTTP Server] modules documentation for more information. You can use both [Gradle] and [Maven] to
build your application.

You can start by cloning a starter project ([Gradle Starter] or [Maven Starter]). Or you can create
a project from scratch following these steps:

1. Configure Kotlin in [Gradle][Setup Gradle] or [Maven][Setup Maven].
2. Add the dependency in Gradle or Maven:

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagontk:http_server_jetty:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagontk</groupId>
      <artifactId>http_server_jetty</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

4. Write the code in the `src/main/kotlin/Hello.kt` file:

@code http/http_server_jetty/src/test/kotlin/com/hexagontk/http/server/jetty/HelloWorldTest.kt?hello_world

5. Run the service and view the results at: [http://localhost:2010/hello/hex][Endpoint]

# Dependencies Verification
Hexagon's dependencies are signed, you can get the public key at the
[Ubuntu Public Key Server][pgp key] or [here][site pgp key].

These are the details of the public key:

```
pub  ed25519/0E16E194 2024-08-24 Hexagon Toolkit (Key used to sign published binaries) <project@hexagontk.com>
     268363E34136BFA0AA6C8AE61902F0990E16E194
```

[pgp key]: https://keyserver.ubuntu.com/pks/lookup?search=project%40hexagontk.com&op=index
[site pgp key]: project_hexagontk_com_public.key

# Next Steps
To continue learning about this toolkit, you can:

* Read the [Core] or [HTTP Server] modules documentation for details on specific modules.
* Clone the [Gradle Starter] or [Maven Starter] repository for a minimal fully working example
  (including tests).
* Proceed to the [Examples] section to check code snippets or full example projects.

[Gradle Starter]: https://github.com/hexagontk/gradle_starter
[Maven Starter]: https://github.com/hexagontk/maven_starter
[Examples]: examples/http_server_examples.md
[Setup Gradle]: https://kotlinlang.org/docs/reference/using-gradle.html
[Setup Maven]: https://kotlinlang.org/docs/reference/using-maven.html
[Gradle]: https://gradle.org
[Maven]: https://maven.apache.org
[Endpoint]: http://localhost:2010/hello
[Core]: core.md
[HTTP Server]: http_server.md
