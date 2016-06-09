package co.there4.hexagon.ratpack

import ratpack.registry.Registry
import ratpack.registry.RegistrySpec
import ratpack.server.RatpackServerSpec
import java.net.InetAddress

class KServerSpec(
    private val delegate: RatpackServerSpec,
    val bindPort: Int? = null,
    val bindAddress: InetAddress? = null
    ) : RatpackServerSpec by delegate {

    fun serverConfig(cb: KServerConfigBuilder.() -> Unit) =
        delegate.serverConfig {
            val kServerConfigBuilder = KServerConfigBuilder(it)
            kServerConfigBuilder.(cb)()
            if (bindPort != null)
                kServerConfigBuilder.port = bindPort
            if (bindAddress != null)
                kServerConfigBuilder.address = bindAddress
        }

    fun registry(cb: RegistrySpec.() -> Unit) = delegate.registry (Registry.of(cb))
    fun handlers(cb: KChain.() -> Unit) = delegate.handlers { KChain(it).(cb)() }
}
