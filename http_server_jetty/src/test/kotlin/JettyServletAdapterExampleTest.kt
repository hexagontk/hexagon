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

internal class JettyServletAdapterBooksTest : BooksTest(adapter)
internal class JettyServletAdapterCookiesTest : CookiesTest(adapter)
internal class JettyServletAdapterSessionTest : SessionTest(adapter)
internal class JettyServletAdapterErrorsTest : ErrorsTest(adapter)
internal class JettyServletAdapterFiltersTest : FiltersTest(adapter)
internal class JettyServletAdapterFilesTest : FilesTest(adapter)
internal class JettyServletAdapterGenericTest : GenericTest(adapter)
internal class JettyServletAdapterTodoTest : TodoTest(adapter)
internal class JettyServletAdapterCorsTest : CorsTest(adapter)
internal class JettyServletAdapterSamplesTest : PortHttpServerSamplesTest(adapter)
internal class JettyServletAdapterHttpsTest : HttpsTest(adapter)
internal class JettyServletAdapterZipTest : ZipTest(adapter)
