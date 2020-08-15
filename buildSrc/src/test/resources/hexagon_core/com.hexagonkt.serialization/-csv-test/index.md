[hexagon_core](../../index.md) / [com.hexagonkt.serialization](../index.md) / [Csv](./index.md)

# Csv

`object Csv : `[`SerializationFormat`](../-serialization-format/index.md)

### Properties

| Name | Summary |
|---|---|
| [contentType](content-type.md) | `val contentType: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string) |
| [extensions](extensions.md) | `val extensions: `[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string)`>` |
| [isBinary](is-binary.md) | `val isBinary: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean) |

### Functions

| Name | Summary |
|---|---|
| [parse](parse.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any)`> parse(input: `[`InputStream`](https://docs.oracle.com/javase/6/docs/api/java/io/InputStream.html)`, type: `[`KClass`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class)`<T>): T` |
| [parseObjects](parse-objects.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any)`> parseObjects(input: `[`InputStream`](https://docs.oracle.com/javase/6/docs/api/java/io/InputStream.html)`, type: `[`KClass`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class)`<T>): `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list)`<T>` |
| [serialize](serialize.md) | `fun serialize(obj: `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any)`, output: `[`OutputStream`](https://docs.oracle.com/javase/6/docs/api/java/io/OutputStream.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit) |
