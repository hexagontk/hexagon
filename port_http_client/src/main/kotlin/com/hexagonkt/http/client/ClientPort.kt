package com.hexagonkt.http.client

interface ClientPort {
    fun send(client: Client, request: Request): Response
}
