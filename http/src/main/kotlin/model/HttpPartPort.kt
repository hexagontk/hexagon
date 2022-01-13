package com.hexagonkt.http.model

interface HttpPartPort : HttpBase {
    val name: String
    val size: Long
    val submittedFileName: String?
}
