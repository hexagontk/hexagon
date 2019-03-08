package com.hexagonkt.http.server.servlet

import io.mockk.every
import io.mockk.mockk
import org.testng.annotations.Test
import javax.servlet.http.HttpServletRequest

@Test class RequestTest {

    @Test fun `Request path is obtained based on servlet path`() {
        val servletRequest = mockk<HttpServletRequest>()
        every { servletRequest.servletPath } returns ""
        every { servletRequest.pathInfo } returns "pathInfo"

        val requestWithoutServletPath = Request(servletRequest)
        assert(requestWithoutServletPath.path == "pathInfo")

        every { servletRequest.servletPath } returns "servletPath"

        val requestWithServletPath = Request(servletRequest)
        assert(requestWithServletPath.path == "servletPath")
    }
}
