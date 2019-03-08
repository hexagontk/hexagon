package com.hexagonkt.http.server.servlet

import com.hexagonkt.helpers.RequiredKeysMap
import com.hexagonkt.http.Path
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

    @Test fun `Request without action path returns empty path parameters`() {
        val servletRequest = mockk<HttpServletRequest>()
        every { servletRequest.servletPath } returns ""
        every { servletRequest.pathInfo } returns "/1/2"

        val request = Request(servletRequest)
        val pathParameters = request.pathParameters
        assert(pathParameters.entries.isEmpty())
    }

    @Test fun `Request path parameters are returned properly`() {
        val servletRequest = mockk<HttpServletRequest>()
        every { servletRequest.servletPath } returns ""
        every { servletRequest.pathInfo } returns "/1/2"

        val request = Request(servletRequest)
        request.actionPath = Path("/{a}/{b}")

        val pathParameters = request.pathParameters
        val requiredKeysMap = RequiredKeysMap(linkedMapOf("a" to "1", "b" to "2"))
        assert(pathParameters.entries == requiredKeysMap.entries)
    }
}
