package com.hexagonkt.injection

import com.hexagonkt.injection.Provider.Generator
import com.hexagonkt.injection.Provider.Instance
import com.hexagonkt.injection.InjectionManager.module
import com.hexagonkt.injection.InjectionManager.injector
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@TestMethodOrder(OrderAnnotation::class)
internal class InjectionManagerTest {

    interface Vehicle {
        val wheels: Int
    }

    data class Bike(override val wheels: Int = 2) : Vehicle
    data class Car(override val wheels: Int = 4) : Vehicle

    @Test fun `Inject not bound class throws exception`() {

        data class Example(val text: String)

        module.bind("ex", Example("An example"))

        assertFailsWith<IllegalStateException> { injector.inject<Test>() }
        assertFailsWith<IllegalStateException> { injector.inject<Example>() }
        assertFailsWith<IllegalStateException> { injector.inject<Example>("ej") }
        assert(injector.inject<Example>("ex") == Example("An example"))
    }

    @Test fun `'injectOrNull' not bound class may return null`() {

        data class Example(val text: String)

        module.bind("ex", Example("An example"))

        assert(injector.injectOrNull<Test>() == null)
        assert(injector.injectOrNull<Example>() == null)
        assert(injector.injectOrNull<Example>("ej") == null)
        assert(injector.injectOrNull<Example>("ex") == Example("An example"))
    }

    @Test
    @Order(1)
    fun `DI don't override bindings`() {
        module.clear()

        fun ignoreException(block: () -> Unit) {
            try {
                block()
            }
            catch (e: IllegalStateException){
                InjectionManager.logger.error(e) { "Attempt to redefine binding not allowed" }
            }
        }

        InjectionManager.apply {
            module.bind<Foo>(::SubFoo1)
            ignoreException { module.bind<Foo>(::SubFoo1) }
        }

        injector.apply {
            module.bind<Foo>(2, ::SubFoo2)
            ignoreException { module.bind<Foo>(2, ::SubFoo2) }

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

            ignoreException { module.bind<Foo>(::SubFoo2) }

            val foo2 = inject(Foo::class)
            assertEquals(SubFoo1::class, foo2::class)

            ignoreException { module.bind<Foo>(SubFoo3) }
            ignoreException { module.bind<Foo> { SubFoo3 } }

            val foo3 = inject(Foo::class)
            assert(foo3.javaClass == SubFoo1::class.java)

            module.bind<Bar> { SubBar1(inject(Foo::class)) }
            ignoreException { module.bind<Bar> { SubBar1(inject()) } }

            val bar1 = inject(Bar::class)
            assert(bar1.javaClass == SubBar1::class.java)
            assert(bar1.foo.javaClass == SubFoo1::class.java)

            ignoreException { module.bind<Bar> { SubBar2() } }

            val bar2 = inject(Bar::class)
            assert(bar2.javaClass == SubBar1::class.java)
            assert(bar2.foo.javaClass == SubFoo1::class.java)

            ignoreException { module.bind<Bar> { SubBar3() } }
            ignoreException { module.bind<Bar>(::SubBar3a) }

            val bar3 = inject<Bar>()
            assert(bar3.javaClass == SubBar1::class.java)
            assert(bar3.foo.javaClass == SubFoo1::class.java)
        }
    }

    @Test
    @Order(2)
    fun `DI just works`() {
        module.clear()

        InjectionManager.apply {
            module.bind<Foo>(::SubFoo1)
        }

        module.bind<Foo>(2, ::SubFoo2)

        injector.apply {
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

            module.forceBind<Foo>(::SubFoo2)
            module.forceBind<Foo>("label", ::SubFoo2)
            module.forceBind<Foo>("label") { SubFoo1() }

            val foo2 = inject(Foo::class)
            val fooLabel = inject(Foo::class, "label")
            assert(foo2.javaClass == SubFoo2::class.java)
            assert(fooLabel.javaClass == SubFoo1::class.java)

            module.forceBind<Foo>(SubFoo3)
            module.bind<Foo>("tag", SubFoo3)
            module.forceBind<Foo>("tag", SubFoo2())

            val foo3 = inject(Foo::class)
            assert(foo3.javaClass == SubFoo3::class.java)
            val foo4 = inject(Foo::class, "tag")
            assert(foo4.javaClass == SubFoo2::class.java)

            module.bind<Bar> { SubBar1(inject(Foo::class)) }

            val bar1 = inject(Bar::class)
            assert(bar1.javaClass == SubBar1::class.java)
            assert(bar1.foo.javaClass == SubFoo3::class.java)

            module.forceBind<Bar> { SubBar2() }

            val bar2 = inject(Bar::class)
            assert(bar2.javaClass == SubBar2::class.java)
            assert(bar2.foo.javaClass == SubFoo3::class.java)

            module.forceBind<Bar>(::SubBar3a)

            val bar3 = inject<Bar>()
            assert(bar3.javaClass == SubBar3a::class.java)
            assert(bar3.foo.javaClass == SubFoo3::class.java)

            assert(module.toString().contains("com.hexagonkt.injection.Foo"))
            assert(module.toString().contains("com.hexagonkt.injection.Bar"))
        }
    }

    @Test
    @Order(3)
    fun `Mocks are easy to build`() {
        var aCalled = false

        module.bind<Service>(object : Service {
            override fun a(p: Int) { aCalled = true }
            override fun b(p: Boolean) = 100
        })

        module.bind<Service>(2, object : Service {
            override fun a(p: Int) { aCalled = true }
            override fun b(p: Boolean) = 200
        })

        val srv = injector.inject<Service>()

        assert(srv.b(true) == 100)
        srv.a(0)
        assert(aCalled)

        val srv2 = injector.inject<Service>(2)

        assert(srv2.b(true) == 200)
    }

    @Test
    @Order(4)
    fun `Bind lists instances works properly`() {
        module.bindings = emptyMap()
        module.bind("switch", true)

        module.bindInstances(Bike(), Car())
        assert(injector.injectList(Vehicle::class) == listOf(Bike(), Car()))
        assert(injector.injectMap(Vehicle::class) == mapOf(0 to Bike(), 1 to Car()))

        module.forceBind(Vehicle::class, listOf(Instance(Car()), Instance(Bike())))
        assert(injector.injectList(Vehicle::class) == listOf(Car(), Bike()))
        assert(injector.injectMap(Vehicle::class) == mapOf(0 to Car(), 1 to Bike()))
    }

    @Test
    @Order(5)
    fun `Bind maps instances works properly`() {
        module.clear()
        module.bind("switch", true)

        module.bindInstances("bike" to Bike(), "car" to Car())
        assert(injector.injectList(Vehicle::class) == listOf(Bike(), Car()))
        assert(injector.injectMap(Vehicle::class) == mapOf("bike" to Bike(), "car" to Car()))

        module.forceBind(
            Vehicle::class, mapOf("car" to Instance(Car()), "bike" to Instance(Bike())))
        assert(injector.injectList(Vehicle::class) == listOf(Car(), Bike()))
        assert(injector.injectMap(Vehicle::class) == mapOf("car" to Car(), "bike" to Bike()))
    }

    @Test
    @Order(6)
    fun `Bind lists functions works properly`() {
        module.clear()
        module.bind("switch", true)

        module.bindGenerators({ Bike() }, { Car() })
        assert(injector.injectList(Vehicle::class) == listOf(Bike(), Car()))
        assert(injector.injectMap(Vehicle::class) == mapOf(0 to Bike(), 1 to Car()))

        module.forceBind(Vehicle::class, listOf(Generator { Car() }, Generator { Bike() }))
        assert(injector.injectList(Vehicle::class) == listOf(Car(), Bike()))
        assert(injector.injectMap(Vehicle::class) == mapOf(0 to Car(), 1 to Bike()))
    }

    @Test
    @Order(7)
    fun `Bind maps functions works properly`() {
        module.clear()
        module.bind("switch", true)

        module.bindGenerators("bike" to { Bike() }, "car" to { Car() })
        assert(injector.injectList(Vehicle::class) == listOf(Bike(), Car()))
        assert(injector.injectMap(Vehicle::class) == mapOf("bike" to Bike(), "car" to Car()))

        module.forceBind(
            Vehicle::class, mapOf("car" to Generator { Car() }, "bike" to Generator { Bike() }))
        assert(injector.injectList(Vehicle::class) == listOf(Car(), Bike()))
        assert(injector.injectMap(Vehicle::class) == mapOf("car" to Car(), "bike" to Bike()))
    }
}
