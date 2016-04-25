package co.there4.hexagon.ratpack

import co.there4.hexagon.template.PebbleRenderer
import ratpack.handling.ByMethodSpec
import ratpack.handling.Context
import java.util.*

/**
 * TODO Support setting content-type depending on template extension
 */
class KContext (val delegate: Context) : Context by delegate {
    fun byMethod (cb: ByMethodSpec.() -> Unit) {
        delegate.byMethod { it.(cb)() }
    }

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
