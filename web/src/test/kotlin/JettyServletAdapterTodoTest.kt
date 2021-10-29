package com.hexagonkt.web

import com.hexagonkt.http.server.jetty.JettyServletAdapter
import com.hexagonkt.web.examples.TodoTest

internal class JettyServletAdapterTodoTest : TodoTest(JettyServletAdapter())
