import org.testng.annotations.Test
import kotlin.reflect.KClass

/*
 * Example interfaces and implementations
 */
interface Foo

class SubFoo1 : Foo
class SubFoo2 : Foo
object SubFoo3 : Foo

interface Bar { val foo: Foo }

class SubBar1(override val foo: Foo) : Bar
class SubBar2(override val foo: Foo = (Foo::class)()) : Bar

/*
 * Generators registry and utilities
 */
private var registry : Map<KClass<*>, () -> Any> = mapOf()

fun <T : Any> register(type: KClass<T>, provider: () -> T) {
    registry += type to provider
}

@Suppress("UNCHECKED_CAST")
operator fun <T : Any> KClass<T>.invoke (): T =
    registry[this]?.invoke() as? T ?: error("${this.java.name} generator not found")

/*
 * Tests
 */
@Test class DiPoC {
    fun di_just_works() {
        register (Foo::class, ::SubFoo1)

        val foo1 = (Foo::class)()
        assert(foo1.javaClass == SubFoo1::class.java)

        register (Foo::class, ::SubFoo2)

        val foo2 = (Foo::class)()
        assert(foo2.javaClass == SubFoo2::class.java)

        register (Foo::class) { SubFoo3 }

        val foo3 = (Foo::class)()
        assert(foo3.javaClass == SubFoo3::class.java)

        register (Bar::class) { SubBar1((Foo::class)()) }

        val bar1 = (Bar::class)()
        assert(bar1.javaClass == SubBar1::class.java)
        assert(bar1.foo.javaClass == SubFoo3::class.java)

        register (Bar::class) { SubBar2() }

        val bar2 = (Bar::class)()
        assert(bar2.javaClass == SubBar2::class.java)
        assert(bar2.foo.javaClass == SubFoo3::class.java)
    }
}
