package com.hexagonkt.http.server.jetty

import com.hexagonkt.http.server.examples.*
import com.hexagonkt.web.examples.TodoTest
import org.testng.annotations.Test

val adapter = JettyServletAdapter()
val asyncAdapter = JettyServletAdapter(true)

@Test class JettyServletAdapterBooksTest : BooksTest(adapter)
@Test class JettyServletAdapterCookiesTest : CookiesTest(adapter)
@Test class JettyServletAdapterSessionTest : SessionTest(adapter)
@Test class JettyServletAdapterErrorsTest : ErrorsTest(adapter)
@Test class JettyServletAdapterFiltersTest : FiltersTest(adapter)
@Test class JettyServletAdapterFilesTest : FilesTest(adapter)
@Test class JettyServletAdapterGenericTest : GenericTest(adapter)
@Test class JettyServletAdapterTodoTest : TodoTest(adapter)

@Test class JettyServletAdapterAsyncBooksTest : BooksTest(asyncAdapter)
@Test class JettyServletAdapterAsyncCookiesTest : CookiesTest(asyncAdapter)
@Test class JettyServletAdapterAsyncSessionTest : SessionTest(asyncAdapter)
@Test class JettyServletAdapterAsyncErrorsTest : ErrorsTest(asyncAdapter)
@Test class JettyServletAdapterAsyncFiltersTest : FiltersTest(asyncAdapter)
@Test class JettyServletAdapterAsyncFilesTest : FilesTest(asyncAdapter)
@Test class JettyServletAdapterAsyncGenericTest : GenericTest(asyncAdapter)
@Test class JettyServletAdapterAsyncTodoTest : TodoTest(asyncAdapter)
