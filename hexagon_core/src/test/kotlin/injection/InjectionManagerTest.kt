package com.hexagonkt.injection

import org.junit.jupiter.api.Test
import com.hexagonkt.injection.InjectionManager.inject
import com.hexagonkt.injection.InjectionManager.bind
import com.hexagonkt.injection.InjectionManager.bindObject
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder
import java.lang.IllegalStateException
import kotlin.test.assertFailsWith
import com.hexagonkt.injection.InjectionManager.bindObjects
import com.hexagonkt.injection.InjectionManager.injectList
import com.hexagonkt.injection.InjectionManager.injectMap
import com.hexagonkt.injection.InjectionManager.injectOrNull

@TestMethodOrder(OrderAnnotation::class)
class InjectionManagerTest {

    @Test fun `Inject not bound class throws exception`() {

        data class Example(val text: String)

        bindObject("ex", Example("An example"))

        assertFailsWith<IllegalStateException> { inject<Test>() }
        assertFailsWith<IllegalStateException> { inject<Example>() }
        assertFailsWith<IllegalStateException> { inject<Example>("ej") }
        assert(inject<Example>("ex") == Example("An example"))
    }

    @Test fun `'injectOrNull' not bound class may return null`() {

        data class Example(val text: String)

        bindObject("ex", Example("An example"))

        assert(injectOrNull<Test>() == null)
        assert(injectOrNull<Example>() == null)
        assert(injectOrNull<Example>("ej") == null)
        assert(injectOrNull<Example>("ex") == Example("An example"))
    }

    @Test
    @Order(1)
    fun `DI don't override bindings`() {
        val injector = InjectionManager.apply {
            bind(Foo::class, ::SubFoo1)
            bind<Foo>(::SubFoo1)
        }

        injector.bind(Foo::class, 2, ::SubFoo2)
        injector.bind<Foo>(2, ::SubFoo2)

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
        assert(foo2.javaClass == SubFoo1::class.java)

        bind(Foo::class) { SubFoo3 }
        bind<Foo> { SubFoo3 }

        val foo3 = inject(Foo::class)
        assert(foo3.javaClass == SubFoo1::class.java)

        bind(Bar::class) { SubBar1(inject(Foo::class)) }
        bind<Bar> { SubBar1(inject()) }

        val bar1 = inject(Bar::class)
        assert(bar1.javaClass == SubBar1::class.java)
        assert(bar1.foo.javaClass == SubFoo1::class.java)

        bind(Bar::class) { SubBar2() }

        val bar2 = inject(Bar::class)
        assert(bar2.javaClass == SubBar1::class.java)
        assert(bar2.foo.javaClass == SubFoo1::class.java)

        bind(Bar::class) { SubBar3() }
        bind(Bar::class, ::SubBar3a)

        val bar3 = inject<Bar>()
        assert(bar3.javaClass == SubBar1::class.java)
        assert(bar3.foo.javaClass == SubFoo1::class.java)
    }

    @Test
    @Order(2)
    fun `DI just works`() {
        val injector = InjectionManager.apply {
            bind(Foo::class, ::SubFoo1)
            bind<Foo>(::SubFoo1)
        }

        injector.bind(Foo::class, 2, ::SubFoo2)
        injector.bind<Foo>(2, ::SubFoo2)

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

        forceBind(Foo::class, ::SubFoo2)
        forceBind<Foo>(::SubFoo2)

        val foo2 = inject(Foo::class)
        assert(foo2.javaClass == SubFoo2::class.java)

        forceBind(Foo::class) { SubFoo3 }
        forceBindObject<Foo>(SubFoo3)
        bindObject<Foo>("tag", SubFoo3)
        forceBindObject<Foo>("tag", SubFoo2())

        val foo3 = inject(Foo::class)
        assert(foo3.javaClass == SubFoo3::class.java)
        val foo4 = inject(Foo::class, "tag")
        assert(foo4.javaClass == SubFoo2::class.java)

        bind(Bar::class) { SubBar1(inject(Foo::class)) }
        bind<Bar> { SubBar1(inject()) }

        val bar1 = inject(Bar::class)
        assert(bar1.javaClass == SubBar1::class.java)
        assert(bar1.foo.javaClass == SubFoo3::class.java)

        forceBind(Bar::class) { SubBar2() }

        val bar2 = inject(Bar::class)
        assert(bar2.javaClass == SubBar2::class.java)
        assert(bar2.foo.javaClass == SubFoo3::class.java)

        forceBind(Bar::class) { SubBar3() }
        forceBind(Bar::class, ::SubBar3a)

        val bar3 = inject<Bar>()
        assert(bar3.javaClass == SubBar3a::class.java)
        assert(bar3.foo.javaClass == SubFoo3::class.java)

        assert(injector.toString().contains("com.hexagonkt.injection.Foo"))
        assert(injector.toString().contains("com.hexagonkt.injection.Bar"))
    }

    @Test
    @Order(3)
    fun `Mocks are easy to build`() {
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

    @Test
    @Order(4)
    fun `Bind lists instances works properly`() {
        InjectionManager.bindings = emptyMap()
        bindObject("switch", true)

        bindObjects(listOf(Bike(), Car()))
        assert(injectList(Vehicle::class) == listOf(Bike(), Car()))
        assert(injectMap(Vehicle::class) == mapOf(0 to Bike(), 1 to Car()))

        forceBindObjects(listOf(Car(), Bike()))
        assert(injectList(Vehicle::class) == listOf(Car(), Bike()))
        assert(injectMap(Vehicle::class) == mapOf(0 to Car(), 1 to Bike()))
    }

    @Test
    @Order(5)
    fun `Bind maps instances works properly`() {
        InjectionManager.bindings = emptyMap()
        bindObject("switch", true)

        bindObjects(mapOf("bike" to Bike(), "car" to Car()))
        assert(injectList(Vehicle::class) == listOf(Bike(), Car()))
        assert(injectMap(Vehicle::class) == mapOf("bike" to Bike(), "car" to Car()))

        forceBindObjects(mapOf("car" to Car(), "bike" to Bike()))
        assert(injectList(Vehicle::class) == listOf(Car(), Bike()))
        assert(injectMap(Vehicle::class) == mapOf("car" to Car(), "bike" to Bike()))
    }

    @Test
    @Order(6)
    fun `Bind lists functions works properly`() {
        InjectionManager.bindings = emptyMap()
        bindObject("switch", true)

        bind(listOf({ Bike() }, { Car() }))
        assert(injectList(Vehicle::class) == listOf(Bike(), Car()))
        assert(injectMap(Vehicle::class) == mapOf(0 to Bike(), 1 to Car()))

        forceBind(listOf({ Car() }, { Bike() }))
        assert(injectList(Vehicle::class) == listOf(Car(), Bike()))
        assert(injectMap(Vehicle::class) == mapOf(0 to Car(), 1 to Bike()))
    }

    @Test
    @Order(7)
    fun `Bind maps functions works properly`() {
        InjectionManager.bindings = emptyMap()
        bindObject("switch", true)

        bind(mapOf("bike" to { Bike() }, "car" to { Car() }))
        assert(injectList(Vehicle::class) == listOf(Bike(), Car()))
        assert(injectMap(Vehicle::class) == mapOf("bike" to Bike(), "car" to Car()))

        forceBind(mapOf("car" to { Car() }, "bike" to { Bike() }))
        assert(injectList(Vehicle::class) == listOf(Car(), Bike()))
        assert(injectMap(Vehicle::class) == mapOf("car" to Car(), "bike" to Bike()))
    }

    interface Vehicle {
        val wheels: Int
    }

    data class Bike(override val wheels: Int = 2) : Vehicle
    data class Car(override val wheels: Int = 4) : Vehicle
}
