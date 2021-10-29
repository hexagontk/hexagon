
## Maven Starters

This module holds the Maven parent POMs with Kotlin setup to ease the project creation using
[Maven](https://maven.apache.org). To use them declare the following `parent` section inside your
`pom.xml` file:

```xml
<parent>
<groupId>com.hexagonkt</groupId>
<artifactId>kotlin_pom</artifactId>
<version>$VERSION</version>
</parent>
```

Or:

```xml
<parent>
<groupId>com.hexagonkt</groupId>
<artifactId>kotlin_lean_pom</artifactId>
<version>$VERSION</version>
</parent>
```
