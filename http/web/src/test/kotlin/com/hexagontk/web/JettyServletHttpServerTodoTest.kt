package com.hexagontk.web

import com.hexagontk.http.server.jetty.JettyServletHttpServer
import com.hexagontk.web.examples.TodoTest

internal class JettyServletHttpServerTodoTest : TodoTest(JettyServletHttpServer())
