package com.hexagonkt.http.server.jetty

import com.hexagonkt.http.server.PortHttpServerSamplesTest
import com.hexagonkt.http.server.examples.*
import com.hexagonkt.web.examples.TodoTest

val adapter = JettyServletAdapter()

class JettyServletAdapterBooksTest : BooksTest(adapter)
class JettyServletAdapterCookiesTest : CookiesTest(adapter)
class JettyServletAdapterSessionTest : SessionTest(adapter)
class JettyServletAdapterErrorsTest : ErrorsTest(adapter)
class JettyServletAdapterFiltersTest : FiltersTest(adapter)
class JettyServletAdapterFilesTest : FilesTest(adapter)
class JettyServletAdapterGenericTest : GenericTest(adapter)
class JettyServletAdapterTodoTest : TodoTest(adapter)
class JettyServletAdapterCorsTest : CorsTest(adapter)
class JettyServletAdapterSamplesTest : PortHttpServerSamplesTest(adapter)
class JettyServletAdapterHttpsTest : HttpsTest(adapter)
class JettyServletAdapterZipTest : ZipTest(adapter)
