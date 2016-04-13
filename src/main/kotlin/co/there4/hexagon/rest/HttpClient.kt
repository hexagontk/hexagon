package co.there4.hexagon.rest

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.net.URL

class HttpClient (val base: URL = URL ("http://localhost:8080")) {
    val JSON = MediaType.parse("application/json; charset=utf-8")

    internal val client = OkHttpClient()

    fun get(url: String): String {
        val request = Request.Builder().url(base.toString() + url).get().build()
        return http(request)
    }

    fun delete(url: String): String {
        val request = Request.Builder().url(base.toString() + url).delete().build()
        return http(request)
    }

    fun post(url: String, json: String): String {
        val body = RequestBody.create(JSON, json)
        val request = Request.Builder().url(base.toString() + url).post(body).build()
        return http(request)
    }

    fun put(url: String, json: String): String {
        val body = RequestBody.create(JSON, json)
        val request = Request.Builder().url(base.toString() + url).put(body).build()
        return http(request)
    }

    fun http(request: Request) = client.newCall(request).execute().body().string()
}
