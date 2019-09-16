
# Simple HTTP service

In this guide, we are going to create a sample HTTP service. You can read the [Core] or
[HTTP Server] modules documentation for more information.

You can start by cloning a starter project ([Gradle Starter] or [Maven Starter]). Or you can create
a project from scratch following these steps:

1. Configure [Kotlin] in [Gradle][Setup Gradle] or [Maven][Setup Maven].
2. Setup the [JCenter] repository (follow the link and click on the `Set me up!` button).
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

@sample hexagon_starters/src/main/kotlin/Service.kt

5. Run the service and view the results at: [http://localhost:2010/hello/world][Endpoint]

You can check the [Developer Guide] for more details. Or you can clone the [Gradle Starter] or
[Maven Starter] for a minimal fully working example (including tests).

[Gradle Starter]: https://github.com/hexagonkt/gradle_starter
[Maven Starter]: https://github.com/hexagonkt/maven_starter
[Kotlin]: https://kotlinlang.org
[Setup Gradle]: https://kotlinlang.org/docs/reference/using-gradle.html
[Setup Maven]: https://kotlinlang.org/docs/reference/using-maven.html
[JCenter]: https://bintray.com/bintray/jcenter
[Gradle]: https://gradle.org
[Maven]: https://maven.apache.org
[Endpoint]: http://localhost:2010/hello/world
[Developer Guide]: /developer_guide/index.html
[Core]: /hexagon_core/index.html
[HTTP Server]: /port_http_server/index.html

# Books Example

A simple CRUD example showing how to manage book resources. Here you can check the
[full test](https://github.com/hexagonkt/hexagon/blob/master/port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/BooksTest.kt).

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/BooksTest.kt:books

# Session Example

Example showing how to use sessions. Here you can check the
[full test](https://github.com/hexagonkt/hexagon/blob/master/port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/SessionTest.kt).

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/SessionTest.kt:session

# Cookies Example

Demo server to show the use of cookies. Here you can check the
[full test](https://github.com/hexagonkt/hexagon/blob/master/port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/CookiesTest.kt).

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/CookiesTest.kt:cookies

# Error Handling Example

Code to show how to handle callback exceptions and HTTP error codes. Here you can check the
[full test](https://github.com/hexagonkt/hexagon/blob/master/port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/ErrorsTest.kt).

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/ErrorsTest.kt:errors

# Filters Example

This example shows how to add filters before and after route execution. Here you can check the
[full test](https://github.com/hexagonkt/hexagon/blob/master/port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/FiltersTest.kt).

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/FiltersTest.kt:filters

# Files Example

The following code shows how to serve resources and receive files. Here you can check the
[full test](https://github.com/hexagonkt/hexagon/blob/master/port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/FilesTest.kt).

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/examples/FilesTest.kt:files
