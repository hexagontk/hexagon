
# Module http_server
This port's purpose is to develop HTTP servers (REST services or Web applications). It defines a DSL
to declare HTTP request handlers.

Adapters implementing this port are in charge of transforming the DSL into a runtime. And allows you
to switch implementations without changing the service.

The [web] module provides utilities on top of this port for Web application development
(like templates helpers).

[web]: /web

### Install the Dependency
This module is not meant to be used directly. You should include and Adapter implementing this
feature (as [http_server_jetty]) in order to create an HTTP server.

[http_server_jetty]: /http_server_jetty

### Server
A server is a process listening to HTTP requests on a TCP port.

You can run multiple ones on different ports at the same time (this can be useful to test many
microservices at the same time).

The server can be configured with different properties. If you do not provide a value for them, they
are searched inside the application settings and lastly, a default value is picked. This is the
parameters list:

* banner: informative text shown at start up logs. If not set only runtime information is displayed.
* bindAddress: address to which this process is bound. If none is provided, `127.0.0.1` is taken.
* bindPort: the port which the process listens to. By default, it is `2010`.
* contextPath: initial path used for the rest of the routes, by default it is empty.

<!-- TODO Link to ServerSettings properties to explain set up. -->
<!-- TODO Document ServerSettings configuration options (options field)). -->

To create a server, you need to provide a router (check the [next section] for more information),
and after creating a server you can run it or stop it with [start()] and [stop()] methods.

@code http_test/src/main/kotlin/examples/SamplesTest.kt:serverCreation

[next section]: /http_server/#routes
[start()]: /api/http_server/com.hexagonkt.http.server/-server/start.html
[stop()]: /api/http_server/com.hexagonkt.http.server/-server/stop.html

#### Servlet Web server
There is a special server adapter for running inside Servlet Containers. To use it you should import
the [Servlet HTTP Server Adapter][http_server_servlet] into your project. Check the
[http_server_servlet] module for more information.

[http_server_servlet]: /http_server_servlet/

### Routes
The main building block of a Hexagon HTTP service is a set of routes. A route is made up of three
simple pieces:

* A **verb** (get, post, put, delete, head, trace, connect, options). It can also be `any`.
* A **path** (/hello, /users/{name}). Paths must start with '/' and trailing slash is ignored.
* A **callback** code block.

<!-- TODO Explain path pattern format -->

The callback has a void return type. You should use `Call.send()` to set the response which will
be returned to the user.

Routes are matched in the order they are defined. The first route that matches the request is
invoked, and the following ones are ignored.

Check the next snippet for usage examples:

@code http_test/src/main/kotlin/examples/SamplesTest.kt:routesCreation

HTTP clients will be able to reuse the routes to create REST services clients.

#### Route groups
Routes can be nested by calling the `path()` method, which takes a String prefix and gives you a
scope to declare routes and filters (or more nested paths). Ie:

@code http_test/src/main/kotlin/examples/SamplesTest.kt:routeGroups

#### Routers
If you have a lot of routes, it can be helpful to group them into routers. You can create routers
to mount a group of routes in different paths (allowing you to reuse them). Check this snippet:

@code http_test/src/main/kotlin/examples/SamplesTest.kt:routers

### Callbacks
Callbacks are request's handling blocks that are bound to routes or filters. They make the request,
response and session objects available to the handling code.

#### Call
The Call object provides you with everything you need to handle a http-request.

It contains the underlying request and response, and a bunch of utility methods to return results,
read parameters or pass attributes among filters/routes.

The methods are available directly from the callback (`Call` is the callback receiver). You can
check the [API documentation] for the full list of methods.

This sample code illustrates the usage:

@code http_test/src/main/kotlin/examples/SamplesTest.kt:callbackCall

[API documentation]: /api/http_server/com.hexagonkt.http.server/-call/

#### Request
Request functionality is provided by the `request` field:

@code http_test/src/main/kotlin/examples/SamplesTest.kt:callbackRequest

#### Path Parameters
Route patterns can include named parameters, accessible via the `pathParameters` map on the request
object:

@code http_test/src/main/kotlin/examples/SamplesTest.kt:callbackPathParam

#### Query Parameters
It is possible to access the whole query string or only a specific query parameter using the
`parameters` map on the `request` object:

@code http_test/src/main/kotlin/examples/SamplesTest.kt:callbackQueryParam

#### Form Parameters
HTML Form processing. Don't parse body!

@code http_test/src/main/kotlin/examples/SamplesTest.kt:callbackFormParam

#### File Uploads
Multipart Requests

@code http_test/src/main/kotlin/examples/SamplesTest.kt:callbackFile

#### Response
Response information is provided by the `response` field:

@code http_test/src/main/kotlin/examples/SamplesTest.kt:callbackResponse

#### Redirects
You can redirect requests (returning 30x codes) by using `Call` utility methods:

@code http_test/src/main/kotlin/examples/SamplesTest.kt:callbackRedirect

#### Cookies
The request and response cookie functions provide a convenient way for sharing information between
handlers, requests, or even servers.

You can read client sent cookies from the request's `cookies` read only map. To change cookies or
add new ones you have to use `response.addCookie()` and `response.removeCookie()` methods.

Check the following sample code for details:

@code http_test/src/main/kotlin/examples/SamplesTest.kt:callbackCookie

#### Sessions
Every request has access to the session created on the server side, the `session` object provides
the following methods:

@code http_test/src/main/kotlin/examples/SamplesTest.kt:callbackSession

<!-- TODO Explain how to set up using server features -->

#### Compression
<!-- TODO Explain how to set up using server features -->

#### Halting
To immediately stop a request within a filter or route use `halt()`. `halt()` is not intended to be
used inside exception-mappers. Check the following snippet for an example:

@code http_test/src/main/kotlin/examples/SamplesTest.kt:callbackHalt

### Filters
You might know filters as interceptors, or middleware from other libraries. Filters are blocks of
code executed before or after one or more routes. They can read the request and read/modify the
response.

All filters that match a route are executed in the order they are declared.

Filters optionally take a pattern, causing them to be executed only if the request path matches
that pattern.

Before and after filters are always executed (if the route is matched). However, any of them may
stop the execution chain if halted.

If `halt()` is called in one filter, filter processing is stopped for that kind of filter (*before*
or *after*). In the case of before filters, this also prevent the route from being executed (but
after filters are executed anyway).

The following code details filters usage:

@code http_test/src/main/kotlin/examples/SamplesTest.kt:filters

### Error Handling
You can provide handlers for runtime errors. Errors are unhandled thrown exceptions in the
callbacks, or handlers halted with an error code.

Error handlers for a given code or exception are unique, and the first one defined is the one which
will be used.

#### HTTP Errors Handlers
Allows handling routes halted with a given code. These handlers are only applied if the route is
halted, if the error code is returned with `send` it won't be handled as an error. Example:

@code http_test/src/main/kotlin/examples/SamplesTest.kt:errors

#### Exception Mapping
You can handle exceptions of a given type for all routes and filters. The handler allows you to
refer to the thrown exception. Look at the following code for a detailed example:

@code http_test/src/main/kotlin/examples/SamplesTest.kt:exceptions

### Static Files
You can use a folder in the classpath for serving static files with the `get()` methods. Note that
the public directory name is not included in the URL.

Asset mapping is handled like any other route, so if an asset mapping is matched, no other route
will be checked (assets or other routes). Also, if a previous route is matched, the asset mapping
will never be checked.

Being `get(resource)` a shortcut of `get("/*", resource)` it should be placed as the last route.
Check the next example for details:

@code http_test/src/main/kotlin/examples/SamplesTest.kt:files

#### MIME types
The MIME types of static files are computed from the file extension using the
[SerializationManager.contentTypeOf()] method.

[SerializationManager.contentTypeOf()]: /api/serialization/com.hexagonkt.serialization/-serialization-manager/content-type-of/

### CORS
CORS behaviour can be different depending on the path. You can attach different [CorsSettings] to
different routers. Check [CorsSettings] class for more details.

@code http_test/src/main/kotlin/examples/CorsTest.kt:cors

[CorsSettings]: /api/http_server/com.hexagonkt.http.server/-cors-settings/

### HTTPS
It is possible to start a secure server enabling HTTPS. For this, you have to provide a server
certificate and its key in the server's [SslSettings]. Once you use a server certificate, it is also
possible to serve content using [HTTP/2], for this to work, [ALPN] is required (however, this is
already handled if you use Java 11).

The certificate common name should match the host that will serve the content in order to be
accepted by an HTTP client without a security error. There is a [Gradle] helper to
[create sample certificates] for development purposes.

HTTP clients can also be configured to use a certificate. This is required to implement a double
ended authorization ([mutual TLS]). This is also done by passing a [SslSettings] object the HTTP
client.

If you want to implement mutual trust, you must enforce client certificate in the server
configuration (check [SslSettings.clientAuth]). If this is done, you can access the certificate the
client used to connect (assuming it is valid, if not the connection will end with an error) with the
[Request.certificateChain] property.

Below you can find a simple example to set up an HTTPS server and client with mutual TLS:

@code http_test/src/main/kotlin/examples/HttpsTest.kt:https

[SslSettings]: /api/http/com.hexagonkt.http/-ssl-settings/
[HTTP/2]: https://en.wikipedia.org/wiki/HTTP/2
[ALPN]: https://en.wikipedia.org/wiki/Application-Layer_Protocol_Negotiation
[Gradle]: https://gradle.org
[create sample certificates]: /gradle/#certificates
[mutual TLS]: https://en.wikipedia.org/wiki/Mutual_authentication
[SslSettings.clientAuth]: /api/http/com.hexagonkt.http/-ssl-settings/client-auth
[Request.certificateChain]: /api/http_server/com.hexagonkt.http.server/-request/certificate-chain

### Testing

#### Integration tests
To test HTTP servers from outside using a real Adapter, you can create a server setting `0` as port.
This will pick a random free port which you can check later:

@code http_test/src/main/kotlin/examples/SamplesTest.kt:test

To do this kind of tests without creating a custom server (using the real production code).
Check the [tests of the starter projects].

[tests of the starter projects]: https://github.com/hexagonkt/gradle_starter/blob/master/src/test/kotlin/GradleStarterTest.kt

If you have an OpenAPI/Swagger spec defined for your server, you can also make use of the mock server ([see below](#openapi-mock-server)).

#### Mocking calls
To unit test callbacks you can create test calls with hardcoded requests, responses and sessions.

To use it in your project you'll have to include a dependency (with test scope):

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    testImplementation("com.hexagonkt:http_server:$hexagonVersion:test")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>http_server</artifactId>
      <version>$hexagonVersion</version>
      <scope>test</scope>
    </dependency>
    ```

Check [testCall], [TestRequest], [TestResponse] and [TestSession] for more details.

For a quick sample, check the snipped below:

TODO Add example code

[testCall]: /api/http_server/com.hexagonkt.http.server.test/test-call/
[TestRequest]: /api/http_server/com.hexagonkt.http.server.test/-test-request/
[TestResponse]: /api/http_server/com.hexagonkt.http.server.test/-test-response/
[TestSession]: /api/http_server/com.hexagonkt.http.server.test/-test-session/

### OpenAPI Mock Server
The mock server is able to create and return a `Server` from the information of an OpenAPI/Swagger spec file.
The path to the spec file can either be a URL or a local file. It can be either in JSON or YAML format.

The mock server takes all its data from the spec and the request and response of the routes is
determined by the routes' examples and schemas.
During handling of a request, the mock server validates the request parameters against those
specified in the spec file and returns an appropriate response from the [provided examples](#providing-examples).
If any authentication requirements are specified, they are validated as well.

#### How to Use
First, add the required dependencies:
```kotlin
implementation("com.hexagonkt:http_server:$hexagonVersion:test")
implementation("io.swagger.parser.v3:swagger-parser:$swaggerParserVersion")
```

To create the mock server object:
```kotlin
val mockServer = MockServer("https://petstore3.swagger.io/api/v3/openapi.json")
```

Optionally, you can also explicitly specify a port number:
```kotlin
val mockServer = MockServer("https://petstore3.swagger.io/api/v3/openapi.json", port = 9090)
```

To get the actual server object:
```kotlin
val server = mockServer.server
```

Finally, to run the server:
```kotlin
server.start()
```

#### OpenAPI Spec File Requirements
The file provided should be a syntactically valid OpenAPI spec file. If it is not, an error will be
raised at initialization time.

For each path in the spec file, descriptions for 200 and 400 (in case parameter verification fails)
status codes must be provided. If the path contains authentication requirements, a 401 status code
description must also be provided. In addition, for each status code, at least one example must be
provided. Note that, at present, only the `application/json` media type is supported.

#### Supported Authentication Methods
Currently, the following Authentication methods/mechanisms are supported:

* API Key authentication (key may be present in the headers, query parameters or cookies)
* HTTP Authentication (Basic or Bearer authentication)

#### Providing Examples
There are several ways to define response examples in an OpenAPI spec file.
If there are multiple examples defined for a particular path status code, you can specify a
particular response by using the `X-Mock-Response-Example` header with the name of the desired example.

The mock server follows the following priority order when parsing the spec file for examples:

1. If the X-Mock-Response-Example header is present, then the example corresponding to the value of
   that header is immediately fetched and returned.
2. If no X-Mock-Response-Example header is present, it first tries to fetch an example from the
   schema key within the media type object.
3. If no example is found here, it then attempts to fetch the example from the example key in the
   media type object.
4. Next, it attempts to fetch the first value found in the examples key of the media type object.
5. If still no example is found, it simply raises an exception.

```json
"content": {
  "application/json": {
    "schema": {
      "example": "This value is checked first"
    },
    "example": "This value is checked second",
    "examples": {
      "example1": {
        "value": "This value is checked third"
      },
      "example2": {
        "value": "This value would be returned if the X-Mock-Response-Example header is passed with value 'example2'"
      }
    }
  }
}
```

# Package com.hexagonkt.http.server
This package defines the classes used in the HTTP DSL.

# Package com.hexagonkt.http.server.callbacks
TODO

# Package com.hexagonkt.http.server.handlers
TODO

# Package com.hexagonkt.http.server.model
TODO
