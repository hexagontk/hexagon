package co.there4.hexagon.rest.ratpack

import ratpack.handling.Context

class KContext (val delegate: Context) : Context by delegate {
    fun template (template: String, context: Map<String, *> = mapOf<String, Any> ()) {
        render (PebbleRenderer.render (template, context))
    }
}
