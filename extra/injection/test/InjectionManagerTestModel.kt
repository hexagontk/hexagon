package com.hexagontk.injection

import com.hexagontk.injection.InjectionManager.injector

internal interface Foo

internal class SubFoo1 : Foo
internal class SubFoo2 : Foo
internal object SubFoo3 : Foo

internal interface Bar { val foo: Foo }

internal class SubBar1(override val foo: Foo) : Bar
internal class SubBar2(override val foo: Foo = injector.inject()) : Bar
internal class SubBar3(override val foo: Foo = injector.inject()) : Bar
internal class SubBar3a(override val foo: Foo) : Bar {
    constructor() : this(injector.inject())
}

internal interface Service {
    fun a(p: Int)
    fun b(p: Boolean): Int
}
