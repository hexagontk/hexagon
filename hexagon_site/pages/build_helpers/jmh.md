
In `build.gradle` it is needed to add the following lines:

```groovy
plugins {
    id 'me.champeau.gradle.jmh' version '0.4.7' apply false
}
```

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
