package com.hexagontk.web

import com.hexagontk.http.server.jetty.JettyServletAdapter
import com.hexagontk.web.examples.TodoTest

internal class JettyServletAdapterTodoTest : TodoTest(JettyServletAdapter())
