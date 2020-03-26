
# Module port_http_client

This port provides a common interface for using HTTP clients. Many adapters can be developed to use
different technologies.

The functionality provided are:

* Security

### Settings

    val contentType: String? = null,
    val useCookies: Boolean = true,
    val headers: Map<String, List<String>> = LinkedHashMap(),
    val user: String? = null,
    val password: String? = null,
    val insecure: Boolean = false,

### Requests

### Responses

### Cookies

### TLS

#### Key Store

#### Trust Store

### Mutual TLS

# Package com.hexagonkt.client

This package holds the classes that define the HTTP client and its configuration settings.
