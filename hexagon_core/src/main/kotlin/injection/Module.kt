//package com.hexagonkt.injection
//
//import java.util.Collections.singletonList
//import kotlin.reflect.KClass
//
//interface Binder<T : Any> {
//    fun bindings(): List<Binding<T>>
//}
//
//data class Binding<T : Any>(
//    val target: Target<T>,
//    val provider: Provider<T>,
//) : Binder<T> {
//
//    override fun bindings(): List<Binding<T>> =
//        singletonList(this)
//}
//
//data class BindingSet<T : Any>(
//    val type: KClass<T>,
//    val providers: Map<Any, Provider<T>>,
//) : Binder<T> {
//
//    override fun bindings(): List<Binding<T>> =
//        providers.map { (k, v) -> Binding(Target(type, k), v) }
//}
//
//class Module {
//    internal var bindings: Map<Target<out Any>, Provider<Any>> = emptyMap()
//
//    fun <T : Any> bind(target: Target<T>, provider: Provider<T>) {
//        bindings = bindings + (target to provider)
//    }
//
//    fun <T : Any> bind(target: Target<T>, instance: T) {
//        bind(target, Instance(instance))
//    }
//
//    fun <T : Any> bind(target: Target<T>, generator: () -> T) {
//        bind(target, Generator(generator))
//    }
//
//    fun <T : Any> bind(type: KClass<T>, instance: T) {
//        bind(Target(type), Instance(instance))
//    }
//
//    fun <T : Any> bind(type: KClass<T>, generator: () -> T) {
//        bind(Target(type), Generator(generator))
//    }
//
//    inline fun <reified T : Any> bind(instance: T, tag: Any = Unit) {
//        bind(Target(T::class, tag), instance)
//    }
//
//    inline fun <reified T : Any> bind(tag: Any = Unit, noinline generator: () -> T) {
//        bind(Target(T::class, tag), generator)
//    }
//
//    // SETS
//
//    fun <T : Any> bindSet(type: KClass<T>, providers: Map<Any, Provider<T>>) {
//        providers.forEach { (k, v) -> bind(Target(type, k) to v) }
//    }
//
//    fun <T : Any> bindSet(type: KClass<T>, providers: List<Provider<T>>) {
//        bindSet(type, providers.mapIndexed { ii, it -> ii to it  }.toMap())
//    }
//
//    fun <T : Any> bindSet(type: KClass<T>, vararg providers: Pair<Any, Provider<T>>) {
//        bindSet(type, providers.toMap())
//    }
//
//    fun <T : Any> bindSet(type: KClass<T>, vararg providers: Provider<T>) {
//        bindSet(type, providers.toList())
//    }
//
//    fun <T : Any> bindSet(type: KClass<T>, vararg providers: T) {
//        bindSet(type, providers.map { Instance(it) })
//    }
//
//    fun <T : Any> bindSet(type: KClass<T>, vararg providers: () -> T) {
//        bindSet(type, providers.map { Generator(it) })
//    }
//
//    inline fun <reified T : Any> bindSet(providers: Map<Any, Provider<T>>) {
//        bindSet(T::class, providers)
//    }
//
//    inline fun <reified T : Any> bindSet(providers: List<Provider<T>>) {
//        bindSet(T::class, providers)
//    }
//
//    inline fun <reified T : Any> bindSet(vararg providers: Pair<Any, Provider<T>>) {
//        bindSet(T::class, providers.toMap())
//    }
//
//    inline fun <reified T : Any> bindSet(vararg providers: Provider<T>) {
//        bindSet(T::class, providers.toList())
//    }
//
//    inline fun <reified T : Any> bindSet(vararg providers: T) {
//        bindSet(T::class, providers.map { Instance(it) })
//    }
//
//    inline fun <reified T : Any> bindSet(vararg providers: () -> T) {
//        bindSet(T::class, providers.map { Generator(it) })
//    }
//}
//
//open class Injector {
//
//    var module: Module = Module()
//
//    @Suppress("UNCHECKED_CAST") // bind operation takes care of type matching
//    fun <T : Any> injectOrNull(type: KClass<T>, tag: Any): T? =
//        module.bindings[Target(type, tag)]?.provide() as? T
//
//    @Suppress("UNCHECKED_CAST") // bind operation takes care of type matching
//    fun <T : Any> injectList(type: KClass<T>): List<T> =
//        module.bindings
//            .filter { it.key.type == type }
//            .map { it.value.provide() as T }
//
//    @Suppress("UNCHECKED_CAST") // bind operation takes care of type matching
//    fun <T : Any> injectMap(type: KClass<T>): Map<Any, T> =
//        module.bindings
//            .filter { it.key.type == type }
//            .map { it.key.tag to it.value.provide() as T }
//            .map { it.first to it.second }
//            .toMap()
//
//    @Suppress("UNCHECKED_CAST") // bind operation takes care of type matching
//    fun <T : Any> inject(type: KClass<T>, tag: Any): T =
//        injectOrNull(type, tag) ?: error("${type.java.name} generator missing")
//
//    inline fun <reified T : Any> inject(tag: Any): T =
//        inject(T::class, tag)
//
//    fun <T : Any> inject(type: KClass<T>): T =
//        inject(type, Unit)
//
//    inline fun <reified T : Any> inject(): T =
//        inject(T::class)
//
//    inline fun <reified T : Any> injectOrNull(tag: Any): T? =
//        injectOrNull(T::class, tag)
//
//    fun <T : Any> injectOrNull(type: KClass<T>): T? =
//        injectOrNull(type, Unit)
//
//    inline fun <reified T : Any> injectOrNull(): T? =
//        injectOrNull(T::class)
//}
