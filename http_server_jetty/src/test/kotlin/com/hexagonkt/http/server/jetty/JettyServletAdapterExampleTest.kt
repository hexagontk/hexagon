package com.hexagonkt.http.server.jetty

import com.hexagonkt.http.server.PortHttpServerSamplesTest
import com.hexagonkt.http.server.examples.*
import com.hexagonkt.web.examples.TodoTest
import org.testng.annotations.Test

val adapter = JettyServletAdapter()

@Test class JettyServletAdapterBooksTest : BooksTest(adapter)
@Test class JettyServletAdapterCookiesTest : CookiesTest(adapter)
@Test class JettyServletAdapterSessionTest : SessionTest(adapter)
@Test class JettyServletAdapterErrorsTest : ErrorsTest(adapter)
@Test class JettyServletAdapterFiltersTest : FiltersTest(adapter)
@Test class JettyServletAdapterFilesTest : FilesTest(adapter)
@Test class JettyServletAdapterGenericTest : GenericTest(adapter)
@Test class JettyServletAdapterTodoTest : TodoTest(adapter)
@Test class JettyServletAdapterCorsTest : CorsTest(adapter)
@Test class JettyServletAdapterSamplesTest : PortHttpServerSamplesTest(adapter)
