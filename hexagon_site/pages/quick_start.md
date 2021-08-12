
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

    implementation("com.hexagonkt:http_server_jetty:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>http_server_jetty</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

4. Write the code in the `src/main/kotlin/Hello.kt` file:

@code http_server_jetty/src/test/kotlin/HelloWorld.kt

5. Run the service and view the results at: [http://localhost:2010/hello][Endpoint]

!!! Tip
    If you use Gradle, you can use the [Application Helper] and run the application watching for
    changes with the command: `./gradlew watch -t`

[Application Helper]: /gradle/#application

# Dependencies Verification

Hexagon's dependencies are signed, you can get the public key at the
[OpenPGP Public Key Server][pgp key] or [here][site pgp key].

These are the details of the public key:

```
pub  4096R/2AEE3721 2020-05-30 Hexagon Toolkit (Key used to sign published binaries) <project@hexagonkt.com>
     Fingerprint=792B D37F F598 91C4 AC6F  8D92 3B26 711D 2AEE 3721
```

!!! Warning
    You may find a deprecated public key in public GPG key servers (check the details
    below). Please, ignore it and use the above one. The information about the obsolete is:

```
pub  2048R/657676D1 2020-05-30 Hexagon Toolkit <project@hexagonkt.com>
     Fingerprint=F263 9BBC 4A6A FE50 D098  9F08 5352 7033 6576 76D1
```

[pgp key]: https://keys.openpgp.org/search?q=project%40hexagonkt.com
[site pgp key]: /project_hexagonkt_com_public.key

# Next Steps

To continue learning about this toolkit, you can:

* Check the [Developer Guide] for more details.
* Clone the [Gradle Starter] or [Maven Starter] repository for a minimal fully working example
  (including tests).
* Proceed to the [Examples] section to check code snippets or full example projects.

[Gradle Starter]: https://github.com/hexagonkt/gradle_starter
[Maven Starter]: https://github.com/hexagonkt/maven_starter
[Examples]: /examples/http_server_examples/
[Setup Gradle]: https://kotlinlang.org/docs/reference/using-gradle.html
[Setup Maven]: https://kotlinlang.org/docs/reference/using-maven.html
[Gradle]: https://gradle.org
[Maven]: https://maven.apache.org
[Endpoint]: http://localhost:2010/hello
[Developer Guide]: /developer_guide/
[Core]: /hexagon_core
[HTTP Server]: /port_http_server
