package com.hexagonkt.injection

import org.testng.annotations.Test
import com.hexagonkt.injection.InjectionManager.inject
import com.hexagonkt.injection.InjectionManager.bind
import com.hexagonkt.injection.InjectionManager.bindObject

class InjectionManagerTest {
    @Test fun di_just_works() {
        bind(Foo::class, ::SubFoo1)
        bind<Foo>(::SubFoo1)

        bind(Foo::class, 2, ::SubFoo2)
        bind<Foo>(2, ::SubFoo2)

        val foo1 = inject(Foo::class)
        assert(foo1.javaClass == SubFoo1::class.java)

        val foo1a = inject<Foo>()
        assert(foo1a.javaClass == SubFoo1::class.java)

        val foo1b: Foo = inject()
        assert(foo1b.javaClass == SubFoo1::class.java)

        val foo12 = inject(Foo::class, 2)
        assert(foo12.javaClass == SubFoo2::class.java)

        val foo12a = inject<Foo>(2)
        assert(foo12a.javaClass == SubFoo2::class.java)

        val foo12b: Foo = inject(2)
        assert(foo12b.javaClass == SubFoo2::class.java)

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

        bindObject<Service>(2, object : Service {
            override fun a(p: Int) { aCalled = true }
            override fun b(p: Boolean) = 200
        })

        val srv = inject<Service>()

        assert(srv.b(true) == 100)
        srv.a(0)
        assert(aCalled)

        val srv2 = inject<Service>(2)

        assert(srv2.b(true) == 200)
    }
}
