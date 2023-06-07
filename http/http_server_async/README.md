
# Module http_server_async
This port's purpose is to develop HTTP servers (REST services or Web applications). It defines a DSL
to declare HTTP request handlers.

Adapters implementing this port are in charge of processing HTTP requests through a list of
handlers.

## Install the Dependency
This module is not meant to be used directly. You should include and Adapter implementing this
feature (as [http_server_jetty]) in order to create an HTTP server.

[http_server_jetty]: /http_server_jetty

# Context on HTTP processing
An HTTP server is nothing more than a function that takes a request and returns a response. Requests
and responses comply with several Web standards.

For the sake of ease of use, HTTP toolkits (or frameworks) are built. These tools make easier to
write an HTTP server that has to deal with different behaviour based on requests attributes.

These development tools usually have different layers/parts (the ones below are some of the most
common ones):

* IO: sockets and buffers management, SSL and thread scheduling is usually handled here also.
* HTTP messages parsing: parse requests to internal model and serialize responses to bytes.
* Routing: makes easy to run different blocks of code based on requests (usually supporting
  pipelining among different blocks).

Hexagon takes care of the third layer, it's "just" an abstraction layer for the IO engine and HTTP
parser underneath, those two layers depends on the adapter (which you can select from a few
alternatives).

This particularity allows users to swap adapters for different use cases. For example, You can use a
low memory for embedded boards (as Raspberry Pi) or high through-output for servers, another use
case would be to use a fast boot adapter for development, and select a different one for production.

To be agnostic of the adapter below, a custom HTTP model is implemented in Hexagon, and adapters
must map their own structures to this model.

Now we'll talk about how HTTP routing is done in the toolkit. The cornerstone of HTTP handling in
Hexagon is the handler: Hexagon handlers are a list of functions that may or may not be applied to
an HTTP call depending on a predicate.

Handlers have a predicate, and they are only applied to a given request if the current request
matches the predicate.

The functions that handle HTTP requests (named 'callbacks' in Hexagon) are blocks of code that get
an HTTP call and return an HTTP call (probably the received one with a modified response),
callbacks operate on immutable structures (the HTTP model).

Below you can find an in deep description of the concepts and components of this toolkit.

# Servers
A server is a process listening to HTTP requests on a TCP port.

You can run multiple ones on different ports at the same time on the same JVM (this can be useful to
test many microservices at the same time).

The server can be [configured with different properties][server settings]. If you do not provide a
value for them, they are searched inside the application settings and lastly, a default value is
picked. This is the parameters list:

* banner: informative text shown at start up logs. If not set only runtime information is displayed.
* bindAddress: address to which this process is bound. If none is provided, `127.0.0.1` is taken.
* bindPort: the port which the process listens to. By default, it is `2010`.
* contextPath: initial path used for the rest of the routes, by default it is empty.

To create a server, you need to provide a handler (check the [handlers section] for more
information), and after creating a server you can run it or stop it with [start()] and [stop()]
methods.

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/SamplesTest.kt?serverCreation

[server settings]: /api/http_server/com.hexagonkt.http.server/-http-server-settings
[handlers section]: /http_server/#handlers
[start()]: /api/http_server/com.hexagonkt.http.server/-http-server/start.html
[stop()]: /api/http_server/com.hexagonkt.http.server/-http-server/stop.html

## Servlet Web server
There is a special server adapter for running inside Servlet Containers. To use it you should import
the Servlet HTTP Server Adapter into your project. Check the [http_server_servlet] module for more
information.

[http_server_servlet]: /http_server_servlet

# HTTP Context
These are the events that the handlers handle (they are also called HTTP calls or just calls along
this documentation). They wrap HTTP requests and responses along with some attributes that may be
used to pass data across handlers or to signal that a previous callback resulted in an error.

The HTTP context provides you with everything you need to handle an HTTP request. It contains the
request, the response, and a bunch of utility methods to return results, read parameters or pass
attributes among handlers.

The methods are available directly from the callback. You can check the [API documentation] for the
full list of methods. This sample code illustrates the usage:

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/SamplesTest.kt?callbackCall

[API documentation]: /api/http_server/com.hexagonkt.http.server.handlers/-http-server-context

# Handlers
The main building blocks of Hexagon HTTP services are a set of handlers. A handler is made up of two
simple pieces:

* A **predicate**: which decides if the handler should be executed for a given request.
* A **callback**: code that receives an HTTP context and returns another (or the same) context.

IMPORTANT: the order in which handlers are declared is NOT the order, it is the depth. Handlers are
not linked, they are NESTED. The [next()][next] method passes control to the next level. If
[next()][next] is not called in a Handler, following Handlers WON'T be executed (However, the
"after" part of already executed Handlers will be run).

For example, this definition:

```
H1
H2
H3
```

Is really this execution:

```
H1 (on)
  H2 (on)
    H3 (on)
  H2 (after)
H1 (after)
```

Check the next snippet for Handlers usage examples:

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/SamplesTest.kt?routesCreation

[next]: /api/http_server/com.hexagonkt.http.server.handlers/-http-server-context/next.html

<!-- TODO Start document review -->

# Handler Predicates
A predicate is a function that is applied to a call context and returns a boolean. If the result is
true, the handler will be executed.

The default implementation ([HttpPredicate]) is based on a template with any combination of
the following fields:

* a list of HTTP methods
* a path pattern
* an exception
* a status

It yields true if all the supplied fields matches a call context.

[HttpPredicate]: /api/http_server/com.hexagonkt.http.handlers/-http-predicate

## Path Patterns
Patterns to match requests paths. They can have:

* Variables: `/path/{param}`
* Wildcards: `/*/path`
* Regular expresión subset: `/(this|that)/path`

# Handler Types

## On Handlers
* Predicate evaluated at start
* Executed at the start (before the 'next' handler is called)

## After Handlers
* Predicate evaluated at end (checked on the coming back of the execution next handler)
* Executed at the end (after 'next' handler has returned)

## Filters
You might know filters as interceptors, or middleware from other libraries. Filters are blocks of
code executed before and/or after other handlers. They can read the request, read/modify the
response, and call the remaining handlers or skip them to halt the call processing.

All filters that match a route are executed in the order they are declared.

Filters optionally take a pattern, causing them to be executed only if the request path matches
that pattern.

The following code details filters usage:

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/SamplesTest.kt?filters

## Path Handlers
Handlers can be grouped by calling the `path()` method, which takes a String prefix and gives you a
scope to declare other handlers. Ie:

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/SamplesTest.kt?routeGroups

If you have a lot of routes, it can be helpful to group them into Path Handlers. You can create path
handlers to mount a group of routes in different paths (allowing you to reuse them). Check this
snippet:

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/SamplesTest.kt?routers

# Handler Callbacks
Callbacks are request's handling blocks that are bound to handlers. They make the request and
response objects available to the handling code.

Callbacks produce a result by returning the received HTTP context with a different response.
Callbacks results are the input for the next handler's callbacks in the pipeline.

## Request
Request functionality is provided by the `request` field:

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/SamplesTest.kt?callbackRequest

## Path Parameters
Route patterns can include named parameters, accessible via the `pathParameters` map on the request
object:

Path parameters can be accessed by name or by index.

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/SamplesTest.kt?callbackPathParam

## Query Parameters
It is possible to access the whole query string or only a specific query parameter using the
`parameters` map on the `request` object:

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/SamplesTest.kt?callbackQueryParam

## Form Parameters
HTML Form processing. Don't parse body!

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/SamplesTest.kt?callbackFormParam

## File Uploads
Multipart Requests

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/SamplesTest.kt?callbackFile

## Response
Response information is provided by the `response` field:

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/SamplesTest.kt?callbackResponse

To send error responses:

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/SamplesTest.kt?callbackHalt

## Redirects
You can redirect requests (returning 30x codes) by using `Call` utility methods:

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/SamplesTest.kt?callbackRedirect

## Cookies
The request and response cookie functions provide a convenient way for sharing information between
handlers, requests, or even servers.

You can read client sent cookies from the request's `cookies` read only map. To change cookies or
add new ones you have to use `response.addCookie()` and `response.removeCookie()` methods.

Check the following sample code for details:

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/SamplesTest.kt?callbackCookie

<!-- TODO End -->

# Error Handling
You can provide handlers for runtime errors. Errors are unhandled exceptions in the callbacks, or
handlers returning error codes.

## HTTP Errors Handlers
Allow handling responses that returned an HTTP error code. Example:

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/SamplesTest.kt?errors

## Exception Mapping
You can handle previously thrown exceptions of a given type (or subtype). The handler allows you to
refer to the thrown exception. Look at the following code for a detailed example:

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/SamplesTest.kt?exceptions

# Static Files
You can use a [FileCallback] or a [UrlCallback] to route requests to files or classpath resources.

Those callbacks can point to folders or to files. If they point to a folder, the pattern should
have a parameter to provide the file to be fetched inside the folder.

Check the next example for details:

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/SamplesTest.kt?files

## Media Types
The media types of static files are computed from the file extension using the utility methods of
the [com.hexagonkt.core.media] package.

[com.hexagonkt.core.media]: /api/core/com.hexagonkt.core.media

# CORS
CORS behaviour can be different depending on the path. You can attach different [CORS Callbacks] to
different handlers. Check the [CorsCallback][CORS Callbacks] class for more details.

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/CorsTest.kt?cors

[CORS Callbacks]: /api/http_server/com.hexagonkt.http.server.callbacks/-cors-callback

# HTTPS
It is possible to start a secure server enabling HTTPS. For this, you have to provide a server
certificate and its key in the server's [SslSettings]. Once you use a server certificate, it is also
possible to serve content using [HTTP/2], for this to work, [ALPN] is required (however, this is
already handled if you use Java 11).

The certificate common name should match the host that will serve the content in order to be
accepted by an HTTP client without a security error. There is a [Gradle] helper to
[create sample certificates] for development purposes.

HTTP clients can also be configured to use a certificate. This is required to implement a double
ended authorization ([mutual TLS]). This is also done by passing an [SslSettings] object the HTTP
client.

If you want to implement mutual trust, you must enforce client certificate in the server
configuration (check [SslSettings.clientAuth]). If this is done, you can access the certificate the
client used to connect (assuming it is valid, if not the connection will end with an error) with the
[Request.certificateChain] property.

Below you can find a simple example to set up an HTTPS server and client with mutual TLS:

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/HttpsTest.kt?https

[SslSettings]: /api/http/com.hexagonkt.http/-ssl-settings
[HTTP/2]: https://en.wikipedia.org/wiki/HTTP/2
[ALPN]: https://en.wikipedia.org/wiki/Application-Layer_Protocol_Negotiation
[Gradle]: https://gradle.org
[create sample certificates]: /gradle/#certificates
[mutual TLS]: https://en.wikipedia.org/wiki/Mutual_authentication
[SslSettings.clientAuth]: /api/http/com.hexagonkt.http/-ssl-settings/client-auth.html
[Request.certificateChain]: /api/http_server/com.hexagonkt.http.server.model/-http-server-request/certificate-chain.html

# WebSockets
A Web Socket is an HTTP(S) connection made with the GET method and the `upgrade: websocket` and
`connection: upgrade` headers.

If the server is handling HTTPS connections, the client should use
the WSS protocol.

When the server receives such a request, it is handled as any other route, and if everything is ok
the connection is converted to a permanent full duplex socket.

If the HTTP request is correct, callbacks should be supplied to handle WS events. Otherwise, if
standard HTTP errors are returned, the connection is closed. This gives the programmer an
opportunity to check the request format or authorization.

Once the WS session is created (after checking the upgrade request is correct), the upgrade request
data can be accessed inside the WS session.

Sessions connected to the same WS endpoint can be stored to broadcast messages.

Ping and pong allows to maintain connection opened.

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/WebSocketsTest.kt?ws_server

# Compression
Gzip encoding is supported on the Hexagon Toolkit, however, its implementation depends on the used
adapter. To turn on Gzip encoding, you need to enable that feature on the server settings. Check the
code below for an example:

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/ZipTest.kt?zip

# Testing

## Integration Tests
To test HTTP servers from outside using a real Adapter, you can create a server setting `0` as port.
This will pick a random free port which you can check later:

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/SamplesTest.kt?test

To do this kind of tests without creating a custom server (using the real production code).
Check the [tests of the starter projects].

[tests of the starter projects]:
https://github.com/hexagonkt/gradle_starter/blob/master/src/test/kotlin/ApplicationTest.kt

## Mocking Calls
To unit test callbacks and handlers you can create test calls with hardcoded requests without
relying on mocking libraries.

For a quick example, check the snipped below:

@code http/http_test/src/main/kotlin/com/hexagonkt/http/test/examples/SamplesTest.kt?mockRequest

# Package com.hexagonkt.http.server.async
This package defines server interfaces for HTTP server adapters.

# Package com.hexagonkt.http.server.callbacks.async
Utility callbacks that can be used on handlers. Reuse a callback in different handlers (after,
filter, etc.).

[http]: /http
