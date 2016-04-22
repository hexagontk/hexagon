package co.there4.hexagon.rest.ratpack

import ratpack.handling.Context
import java.util.*

/**
 * TODO Support setting content-type depending on template extension
 */
class KContext (val delegate: Context) : Context by delegate {
    fun template (
        template: String,
        locale: Locale = Locale.getDefault(),
        context: Map<String, *> = mapOf<String, Any> ()) {

        render (PebbleRenderer.render (template, locale, context))
    }

    fun template (template: String, context: Map<String, *> = mapOf<String, Any> ()) {
        template (template, Locale.getDefault(), context)
    }
}
