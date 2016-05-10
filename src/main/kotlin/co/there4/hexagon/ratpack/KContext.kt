package co.there4.hexagon.ratpack

import co.there4.hexagon.template.PebbleRenderer
import ratpack.file.MimeTypes
import ratpack.form.Form
import ratpack.handling.ByMethodSpec
import ratpack.handling.Context
import ratpack.http.TypedData
import java.nio.charset.Charset.defaultCharset
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
import java.util.*

class KContext (private val delegate: Context) : Context by delegate {
    fun httpDate (date: LocalDateTime) =
        RFC_1123_DATE_TIME.format(ZonedDateTime.of(date, ZoneId.of("GMT")))

    fun byMethod (cb: ByMethodSpec.() -> Unit) {
        delegate.byMethod { it.(cb)() }
    }

    fun template (
        template: String,
        locale: Locale = Locale.getDefault(),
        context: Map<String, *> = mapOf<String, Any> ()) {

        val contentType = get(MimeTypes::class.java).getContentType(template) ?: "text/html"
        response.contentTypeIfNotSet("$contentType; charset=${defaultCharset().name()}")
        render (PebbleRenderer.render (template, locale, context))
    }

    fun template (template: String, context: Map<String, *> = mapOf<String, Any> ()) {
        template (template, Locale.getDefault(), context)
    }

    fun withBody(callback: TypedData.() -> Unit) {
        request.body.then { it.(callback)() }
    }

    fun withForm(callback: Form.() -> Unit) {
        context.parse(Form::class.java).then { it.(callback)() }
    }

    fun send (body: String = "", status: Int = 200) {
        response.status (status)
        if (body == "")
            response.send()
        else
            response.send(body)
    }

    fun ok (body: String = "", status: Int = 200) = send (body, status)
    fun ok (status: Int = 200) = ok("", status)
    fun halt (body: String = "", status: Int = 500) = send (body, status)
    fun halt (status: Int = 500) = halt("", status)
}
