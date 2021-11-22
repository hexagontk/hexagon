package com.hexagonkt.serialization

import com.hexagonkt.core.helpers.MapResourceBundle
import java.net.URL

/**
 * [TODO](https://github.com/hexagonkt/hexagon/issues/271).
 *
 * @property url .
 */
abstract class UrlResourceBundle(val url: URL) : MapResourceBundle(url.parse()) {

    override fun getContents(): Array<Array<Any?>> =
        data
}
