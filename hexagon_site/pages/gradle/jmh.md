
JMH
===

To use it you should add `apply from: $gradleScripts/jmh.gradle` to your `build.gradle` script
and `id 'me.champeau.gradle.jmh' version 'VERSION'` to your `plugins` section in the root
`build.gradle`.

Sample benchmark (src/jmh/kotlin/Benchmark.kt):

```kotlin
import org.openjdk.jmh.annotations.Benchmark

open class Benchmark {
    @Benchmark fun foo() {
        println("foo bench")
        Thread.sleep(100L)
    }

    @Benchmark fun bar() {
        println("bar bench")
        Thread.sleep(100L)
    }
}
```
