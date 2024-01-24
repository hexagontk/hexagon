package com.hexagonkt.http.handlers

// TODO Use this type to implement the RFC 7807 exception handler
interface HttpExceptionCallback<T> : (HttpContext, T) -> HttpContext
