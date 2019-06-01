
# Module port_http_server

This port's purpose is to develop HTTP server processes (REST services or Web applications).
It defines a DSL to declare HTTP request handlers.

Adapters implementing this port are responsible of transforming the DSL to a runtime. And allows you
to switch implementations without changing the service.

The [hexagon_web] module provides utilities on top of this port for Web application development
(like template helpers).

[hexagon_web]: /hexagon_web

### Server

A server is a process listening to HTTP requests in a TCP port.

You can run multiple ones on different ports at the same time (this is useful to test many
microservices at the same time).

The server can be configured with different properties. If you do not provide a value for them, they
are searched inside the application settings and lastly, a default value is picked. This is the
parameters list:

* serviceName: name of this service, it is only informative and it is displayed on the logs. If not
  set `<undefined>` is used.
* bindAddress: address to which this process is bound. If none is provided, `127.0.0.1` is taken.
* bindPort: `2010`

You can inject an adapter for the `Server` port using the [InjectionManager]:
`InjectionManager.bindObject<ServerPort>(JettyServletAdapter())`

To create a server, you need to provide a router (check the [next section] for more information),
and after creating a server you can run it or stop it with [start()] and [stop()]

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

The Handler interface has a void return type. You use `Call.send()` to set the response which will
be returned to the user.

Routes are matched in the order they are defined. The first route that matches the request is
invoked.

Check the next snippet for usage examples:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:routesCreation

HTTP clients will be able to reuse the routes to create REST services clients.

#### Route groups

Routes can be nested by calling the `path()` method, which takes a String prefix and gives you a
scope to declare routes and filters (or nested paths). Ie:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:routeGroups

#### Routers

If you have a lot of routes, it can be helpful to group them into routers. You can create routers
to mount a group of routes in different paths (allowing you to reuse them). Check this snippet:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:routers

### Callbacks

Callbacks are request's handling code that are bound to routes or filters.

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

Request information and functionality is provided by the `request` parameter:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:callbackRequest

#### Response

Response information and functionality is provided by the `response` parameter:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:callbackResponse

#### Path Parameters

Route patterns can include named parameters, accessible via the `pathParameters` map on the request
object:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:callbackPathParam

#### Query Parameters

It is possible to access the whole query string and also access an specific query parameter using
the `parameters` map on the `request` object:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:callbackQueryParam

#### Redirects

You can redirect requests (returning 30x codes) by using `Call` utility methods:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:callbackRedirect

#### Cookies

The ctx.cookieStore() functions provide a convenient way for sharing information between handlers,
request, or even servers:

The cookieStore works like this:

1. The first handler that matches the incoming request will populate the cookie-store-map with the
   data currently stored in the cookie (if any).
2. This map can now be used as a state between handlers on the same request-cycle, pretty much in
   the same way as ctx.attribute()
3. At the end of the request-cycle, the cookie-store-map is serialized, base64-encoded and written
   to the response as a cookie. This allows you to share the map between requests and servers (in
   case you are running multiple servers behind a load-balancer)

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:callbackCookie

#### Sessions

Every request has access to the session created on the server side, provided with the following
methods:

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:callbackSession

#### Halting

To immediately stop a request within a filter or route use halt():

halt() is not intended to be used inside exception-mappers.

@sample port_http_server/src/test/kotlin/com/hexagonkt/http/server/PortHttpServerSamplesTest.kt:callbackHalt

### Filters

Before-filters are evaluated before each request, and can read the request and read/modify the
response.

To stop execution, use halt():

After-filters are evaluated after each request, and can read the request and read/modify the
response:

Filters optionally take a pattern, causing them to be evaluated only if the request path matches
that pattern:

Before and after filters are always executed (if the route is matched). But any of them may stop
the execution chain if halted.

You might know before-handlers as filters, interceptors, or middleware from other libraries.

Before-handlers are matched before every request (including static files, if you enable those).

### Error Handling

#### HTTP Errors Handlers

HTTP status codes handling

#### Exception Mapping

To handle exceptions of a configured type for all routes and filters:

### Multipart Requests

#### HTML Form processing

#### File Uploads

### Static Files

You can assign a folder in the classpath serving static files with the staticFiles.location()
method. Note that the public directory name is not included in the URL.

A file /public/css/style.css is made available as http://{host}:{port}/css/style.css

You can also assign an external folder (a folder not in the classpath) to serve static files by
using the staticFiles.externalLocation() method.\

Static files location must be configured before route mapping. If your application has no routes,
init() must be called manually after location is set.

#### MIME types

### Testing

# Package com.hexagonkt.http.server

This package defines the classes used in the HTTP DSL.
