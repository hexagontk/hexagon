import org.testng.annotations.Test
import kotlin.reflect.KClass

interface Foo
class Foo1 : Foo
class Foo2 : Foo
object Foo3 : Foo

interface Bar { val foo: Foo }
class Bar1(override val foo: Foo) : Bar

var registry : Map<KClass<*>, () -> Any> = mapOf (
    Foo::class to ::Foo1
)

fun <T : Any> register(type: KClass<T>, provider: () -> T) {
    registry += type to provider
}

@Suppress("UNCHECKED_CAST")
operator fun <T : Any> KClass<T>.invoke (): T = registry[this]?.invoke() as T

@Test class DiPoC {
    fun di_just_works() {
        val foo1 = (Foo::class)()
        assert(foo1.javaClass == Foo1::class.java)

        register (Foo::class, ::Foo2)

        val foo2 = (Foo::class)()
        assert(foo2.javaClass == Foo2::class.java)

        register (Foo::class) { Foo3 }

        val foo3 = (Foo::class)()
        assert(foo3.javaClass == Foo3::class.java)

        register (Bar::class) { Bar1((Foo::class)()) }

        val bar1 = (Bar::class)()
        assert(bar1.javaClass == Bar1::class.java)
        assert(bar1.foo.javaClass == Foo3::class.java)
    }
}
