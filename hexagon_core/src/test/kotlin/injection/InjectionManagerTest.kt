package com.hexagonkt.injection

import org.testng.annotations.Test

/*
 * Example interfaces and implementations
 */
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

/*
 * Tests
 */
class DiPoC {
    @Test fun di_just_works() {
        bind(Foo::class, ::SubFoo1)
        bind<Foo>(::SubFoo1)

        val foo1 = inject(Foo::class)
        assert(foo1.javaClass == SubFoo1::class.java)

        val foo1a = inject<Foo>()
        assert(foo1a.javaClass == SubFoo1::class.java)

        val foo1b: Foo = inject()
        assert(foo1b.javaClass == SubFoo1::class.java)

        bind(Foo::class, ::SubFoo2)
        bind<Foo>(::SubFoo2)

        val foo2 = inject(Foo::class)
        assert(foo2.javaClass == SubFoo2::class.java)

        bind (Foo::class) { SubFoo3 }
        bind<Foo> { SubFoo3 }

        val foo3 = inject(Foo::class)
        assert(foo3.javaClass == SubFoo3::class.java)

        bind (Bar::class) { SubBar1(inject(Foo::class)) }
        bind<Bar> { SubBar1(inject()) }

        val bar1 = inject(Bar::class)
        assert(bar1.javaClass == SubBar1::class.java)
        assert(bar1.foo.javaClass == SubFoo3::class.java)

        bind (Bar::class) { SubBar2() }

        val bar2 = inject(Bar::class)
        assert(bar2.javaClass == SubBar2::class.java)
        assert(bar2.foo.javaClass == SubFoo3::class.java)

        bind (Bar::class) { SubBar3() }
        bind (Bar::class, ::SubBar3a)

        val bar3 = inject<Bar>()
        assert(bar3.javaClass == SubBar3a::class.java)
        assert(bar3.foo.javaClass == SubFoo3::class.java)
    }

    @Test fun mocks_are_easy_to_build() {
        var aCalled = false

        bindObject<Service>(object : Service {
            override fun a(p: Int) { aCalled = true }
            override fun b(p: Boolean) = 100
        })

        val srv = inject<Service>()

        assert(srv.b(true) == 100)
        srv.a(0)
        assert(aCalled)
    }
}
