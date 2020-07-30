[hexagon_core](../index.md) / [com.hexagonkt.serialization](./index.md)

## Package com.hexagonkt.serialization

Parse/serialize data in different formats to class instances.

### Types

| Name | Summary |
|---|---|
| [ContentType](-content-type/index.md) | `data class ContentType` |
| [Csv](-csv/index.md) | `object Csv : `[`SerializationFormat`](-serialization-format/index.md) |
| [Json](-json.md) | `object Json : `[`SerializationFormat`](-serialization-format/index.md) |
| [SerializationFormat](-serialization-format/index.md) | `interface SerializationFormat` |
| [SerializationManager](-serialization-manager/index.md) | TODO`object SerializationManager` |
| [Yaml](-yaml.md) | `object Yaml : `[`SerializationFormat`](-serialization-format/index.md) |

### Exceptions

| Name | Summary |
|---|---|
| [ParseException](-parse-exception/index.md) | `class ParseException : `[`RuntimeException`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-runtime-exception) |

### Extensions for External Classes

| Name | Summary |
|---|---|
| [java.io.File](java.io.-file/index.md) |  |
| [java.io.InputStream](java.io.-input-stream/index.md) |  |
| [java.net.URL](java.net.-u-r-l/index.md) |  |
| [kotlin.Any](kotlin.-any/index.md) |  |
| [kotlin.collections.List](kotlin.collections.-list/index.md) |  |
| [kotlin.collections.Map](kotlin.collections.-map/index.md) |  |
| [kotlin.String](kotlin.-string/index.md) |  |

### Functions

| Name | Summary |
|---|---|
| [parse](parse.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any)`> `[`Resource`](../com.hexagonkt.helpers/-resource/index.md)`.parse(type: `[`KClass`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class)`<T>): T`<br>`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any)`> `[`Resource`](../com.hexagonkt.helpers/-resource/index.md)`.parse(): T` |
| [parseObjects](parse-objects.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any)`> `[`Resource`](../com.hexagonkt.helpers/-resource/index.md)`.parseObjects(type: `[`KClass`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class)`<T>): `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list)`<T>`<br>`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any)`> `[`Resource`](../com.hexagonkt.helpers/-resource/index.md)`.parseObjects(): `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list)`<T>` |
