package co.there4.hexagon.ratpack

import co.there4.hexagon.template.PebbleRenderer
import ratpack.file.MimeTypes
import ratpack.handling.ByMethodSpec
import ratpack.handling.Context
import java.util.*

class KContext (private val delegate: Context) : Context by delegate {
    fun byMethod (cb: ByMethodSpec.() -> Unit) {
        delegate.byMethod { it.(cb)() }
    }

    fun template (
        template: String,
        locale: Locale = Locale.getDefault(),
        context: Map<String, *> = mapOf<String, Any> ()) {

        val contentType = get(MimeTypes::class.java).getContentType(template) ?: "text/html"
        response.headers["Content-Type"] = contentType
        render (PebbleRenderer.render (template, locale, context))
    }

    fun template (template: String, context: Map<String, *> = mapOf<String, Any> ()) {
        template (template, Locale.getDefault(), context)
    }

    fun send (body: String = "", contentType: String = "text/plain", status: Int = 200) {
        response.status (status)
        if (body == "")
            response.send()
        else
            response.send(contentType, body)
    }

    fun ok (body: String = "", contentType: String = "text/plain", status: Int = 200) =
        send (body, contentType, status)

    fun ok (status: Int = 200) = ok("", "", status)

    fun halt (body: String = "", contentType: String = "text/plain", status: Int = 500) =
        send (body, contentType, status)

    fun halt (status: Int = 500) = halt("", "", status)
}
