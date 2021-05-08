package com.hexagonkt.http.server.jetty

import com.hexagonkt.http.server.PortHttpServerSamplesTest
import com.hexagonkt.http.server.examples.BooksTest
import com.hexagonkt.http.server.examples.CookiesTest
import com.hexagonkt.http.server.examples.CorsTest
import com.hexagonkt.http.server.examples.ErrorsTest
import com.hexagonkt.http.server.examples.FilesTest
import com.hexagonkt.http.server.examples.FiltersTest
import com.hexagonkt.http.server.examples.GenericTest
import com.hexagonkt.http.server.examples.HttpsTest
import com.hexagonkt.http.server.examples.SessionTest
import com.hexagonkt.http.server.examples.ZipTest
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
