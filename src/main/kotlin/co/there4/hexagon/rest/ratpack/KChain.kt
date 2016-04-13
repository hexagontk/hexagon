package co.there4.hexagon.rest.ratpack

import ratpack.handling.Chain
import ratpack.handling.Context

class KChain (val delegate: Chain) : Chain by delegate {
    fun fileSystem(path: String = "", cb: KChain.() -> Unit) =
        delegate.fileSystem (path) { KChain(it).(cb)() }
    fun prefix(path: String = "", cb: KChain.() -> Unit) =
        delegate.prefix (path) { KChain(it).(cb)() }

    fun all(cb: Context.() -> Unit) = delegate.all { it.(cb)() }
    fun path(path: String = "", cb: Context.() -> Unit) = delegate.path (path) { it.(cb)() }

    @Suppress("ReplaceGetOrSet")
    fun get(path: String = "", cb: Context.() -> Unit) = delegate.get (path) { it.(cb)() }
    fun put(path: String = "", cb: Context.() -> Unit) = delegate.put (path) { it.(cb)() }
    fun post(path: String = "", cb: Context.() -> Unit) = delegate.post (path) { it.(cb)() }
    fun delete(path: String = "", cb: Context.() -> Unit) = delegate.delete (path) { it.(cb)() }
    fun options(path: String = "", cb: Context.() -> Unit) = delegate.options (path) { it.(cb)() }
    fun patch(path: String = "", cb: Context.() -> Unit) = delegate.patch (path) { it.(cb)() }
}
