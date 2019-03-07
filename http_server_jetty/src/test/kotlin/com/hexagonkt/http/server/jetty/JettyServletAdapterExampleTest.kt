package com.hexagonkt.http.server.jetty

import com.hexagonkt.http.server.examples.*
import org.testng.annotations.Test

val adapter = JettyServletAdapter()
val asyncAdapter = JettyServletAdapter(true)

@Test class JettyServletAdapterBooksTest : BooksTest(adapter)
@Test class JettyServletAdapterCookiesTest : CookiesTest(adapter)
@Test class JettyServletAdapterGenericTest : GenericTest(adapter)
@Test class JettyServletAdapterHexagonTest : HexagonTest(adapter)
@Test class JettyServletAdapterSessionTest : SessionTest(adapter)

@Test class JettyServletAdapterAsyncBooksTest : BooksTest(asyncAdapter)
@Test class JettyServletAdapterAsyncCookiesTest : CookiesTest(asyncAdapter)
@Test class JettyServletAdapterAsyncGenericTest : GenericTest(asyncAdapter)
@Test class JettyServletAdapterAsyncHexagonTest : HexagonTest(asyncAdapter)
@Test class JettyServletAdapterAsyncSessionTest : SessionTest(asyncAdapter)
