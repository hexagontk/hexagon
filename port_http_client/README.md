
# Module port_http_client

This port provides a common interface for using HTTP clients. Many adapters can be developed to use
different technologies.

Its main functionalities are:

* HTTP, HTTPS and HTTP/2 support
* Mutual TLS
* Body encoding/decoding
* Request/response exchange
* Form submissions
* Cookie management
* File uploading/downloading

### Install the Dependency

This module is not meant to be used directly. You should include and Adapter implementing this
feature (as [http_client_ahc]) in order to create HTTP clients.

[http_client_ahc]: /http_client_ahc

### Create an HTTP client

You create an HTTP Client instance with default options as follows:

@sample port_http_client/src/test/kotlin/ClientTest.kt:clientCreation

### Settings

If you want to configure options for the client, you create it with the following code:

@sample port_http_client/src/test/kotlin/ClientTest.kt:clientSettingsCreation

### Send simple requests

### Requests

### Responses

### Authorization

### Cookies

### TLS

#### Key Store

#### Trust Store

### Mutual TLS

# Package com.hexagonkt.http.client

This package holds the classes that define the HTTP client and its configuration settings.
