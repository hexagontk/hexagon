package com.hexagonkt.injection

import com.hexagonkt.injection.InjectionManager.inject

interface Foo

class SubFoo1 : Foo
class SubFoo2 : Foo
object SubFoo3 : Foo

interface Bar { val foo: Foo }

class SubBar1(override val foo: Foo) : Bar
class SubBar2(override val foo: Foo = inject()) : Bar
class SubBar3(override val foo: Foo = inject()) : Bar
class SubBar3a(override val foo: Foo) : Bar {
    constructor() : this(inject())
}

interface Service {
    fun a(p: Int)
    fun b(p: Boolean): Int
}

class FakeService : Service {

    override fun a(p: Int) {
        TODO("not implemented")
    }

    override fun b(p: Boolean): Int {
        TODO("not implemented")
    }
}
