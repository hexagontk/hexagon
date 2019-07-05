
# Module port_http_server

This port's purpose is to develop HTTP servers (REST services or Web applications). It defines a DSL
to declare HTTP request handlers.

Adapters implementing this port are responsible of transforming the DSL into a runtime. And allows
you to switch implementations without changing the service.

The [hexagon_web] module provides utilities on top of this port for Web application development
(like templates helpers).

[hexagon_web]: /hexagon_web

### Server

A server is a process listening to HTTP requests on a TCP port.

You can run multiple ones on different ports at the same time (this can be useful to test many
microservices at the same time).

The server can be configured with different properties. If you do not provide a value for them, they
are searched inside the application settings and lastly, a default value is picked. This is the
parameters list:

* serviceName: name of this service, it is only informative and it is displayed on the logs. If not
  set `<undefined>` is used.
* bindAddress: address to which this process is bound. If none is provided, `127.0.0.1` is taken.
* bindPort: the port that the process listens to. By default it is `2010`
* contextPath: initial path used for the rest of the routes, by default it is empty.

You can inject an adapter for the `Server` port using the [InjectionManager] object:
`InjectionManager.bindObject<ServerPort>(JettyServletAdapter())`

To create a server, you need to provide a router (check the [next section] for more information),
and after creating a server you can run it or stop it with [start()] and [stop()] methods.

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:serverCreation

[InjectionManager]: /hexagon_core/#dependency-injection
[next section]: /port_http_server/#routes
[start()]: com.hexagonkt.http.server/-server/start.md
[stop()]: com.hexagonkt.http.server/-server/stop.md

#### Servlet Web server

There is an special server adapter for running inside Servlet Containers. To use it you should
import the [Servlet HTTP Server Adapter][http_server_servlet] into your project. Check the
[http_server_servlet] module for more information.

[http_server_servlet]: /http_server_servlet

### Routes

The main building block of a Hexagon HTTP service is a set of routes. A route is made up of three
simple pieces:

* A **verb** (get, post, put, delete, head, trace, connect, options). It can also be `any`.
* A **path** (/hello, /users/{name}). Paths must start with '/' and trailing slash is ignored.
* A **callback** code block.

The callback has a void return type. You should use `Call.send()` to set the response which will
be returned to the user.

Routes are matched in the order they are defined. The first route that matches the request is
invoked and the following ones are ignored.

Check the next snippet for usage examples:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:routesCreation

HTTP clients will be able to reuse the routes to create REST services clients.

#### Route groups

Routes can be nested by calling the `path()` method, which takes a String prefix and gives you a
scope to declare routes and filters (or more nested paths). Ie:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:routeGroups

#### Routers

If you have a lot of routes, it can be helpful to group them into routers. You can create routers
to mount a group of routes in different paths (allowing you to reuse them). Check this snippet:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:routers

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

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:callbackCall

[API documentation]: /port_http_server/com.hexagonkt.http.server/-call/

#### Request

Request functionality is provided by the `request` field:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:callbackRequest

#### Response

Response information is provided by the `response` field:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:callbackResponse

#### Path Parameters

Route patterns can include named parameters, accessible via the `pathParameters` map on the request
object:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:callbackPathParam

#### Query Parameters

It is possible to access the whole query string or only an specific query parameter using the
`parameters` map on the `request` object:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:callbackQueryParam

#### Redirects

You can redirect requests (returning 30x codes) by using `Call` utility methods:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:callbackRedirect

#### Cookies

The request and response cookie functions provide a convenient way for sharing information between
handlers, requests, or even servers.

You can read client sent cookies from the request's `cookies` read only map. To change cookies or
add new ones you have to use `response.addCookie()` and `response.removeCookie()` methods.

Check the following sample code for details:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:callbackCookie

#### Sessions

Every request has access to the session created on the server side, the `session` object provides
the following methods:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:callbackSession

#### Halting

To immediately stop a request within a filter or route use `halt()`. `halt()` is not intended to be
used inside exception-mappers. Check the following snippet for an example:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:callbackHalt

### Filters

You might know filters as interceptors, or middleware from other libraries. Filters are blocks of
code executed before or after one or more routes. They can read the request and read/modify the
response.
 
All filters that match a route are executed in the order they are declared.

Filters optionally take a pattern, causing them to be executed only if the request path matches
that pattern.

Before and after filters are always executed (if the route is matched). But any of them may stop
the execution chain if halted.

If `halt()` is called in one filter, filter processing is stopped for that kind of filter (*before*
or *after*). In the case of before filters, this also prevent the route from being executed (but
after filters are executed anyway).

The following code details filters usage:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:filters

### Error Handling

You can provide handlers for runtime errors. Errors are unhandled thrown exceptions in the
callbacks, or handlers halted with an error code.

Error handlers for a given code or exception are unique, and the first one defined is the one that
will be used.

#### HTTP Errors Handlers

Allows to handle routes halted with a given code. These handlers are only applied if the route is
halted, if the error code is returned with `send` it won't be handled as an error. Example:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:errors

#### Exception Mapping

You can handle exceptions of a given type for all routes and filters. The handler allows you to
refer to the thrown exception. Look at the following code for a detailed example:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:exceptions

<!--
### Multipart Requests

#### HTML Form processing

#### File Uploads
-->

### Static Files

You can use a folder in the classpath for serving static files with the `assets()` method. Note that
the public directory name is not included in the URL.

Asset mapping is handled like any other route, so if an asset mapping is matched, no other route
will be checked (assets or other routes). And also, if a previous route is matched, the asset
mapping will never be checked.

Being `assets(resource)` a shortcut of `assets(resource, "/*")` it should be placed as the last
route. Check the next example for details:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:files

#### MIME types

The MIME types of static files are computed from the file extension using the
[SerializationManager.contentTypeOf()] method.

[SerializationManager.contentTypeOf()]: /hexagon_core/com.hexagonkt.serialization/-serialization-manager/content-type-of/

### Testing

To test HTTP servers from outside using a real Adapter, you can create a server setting `0` as port.
This will pick a random free port that you can check later:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:test

To do this kind of tests without creating a custom server (using the real production code).
Check the [tests of the starter projects].

[tests of the starter projects]: https://github.com/hexagonkt/gradle_starter/blob/master/src/test/kotlin/GradleStarterTest.kt

# Package com.hexagonkt.http.server

This package defines the classes used in the HTTP DSL.
