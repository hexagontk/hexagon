
# Module http_test
This module...

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    testImplementation("com.hexagonkt:http_test:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>http_test</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

### OpenAPI Mock Server
The mock server is able to create and return a `Server` from the information of an OpenAPI/Swagger spec file.
The path to the spec file can either be a URL or a local file. It can be either in JSON or YAML format.

The mock server takes all its data from the spec and the request and response of the routes is
determined by the routes' examples and schemas.
During handling of a request, the mock server validates the request parameters against those
specified in the spec file and returns an appropriate response from the [provided examples](#providing-examples).
If any authentication requirements are specified, they are validated as well.

#### How to Use
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

# Package com.hexagonkt.http.test
TODO

# Package com.hexagonkt.http.test.examples
TODO

# Package com.hexagonkt.http.test.openapi
TODO
