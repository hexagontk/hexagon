
// NOTE: This import is *NEEDED* for the `HYPHENATED` and `CAMEL_CASE` references
import static uk.co.cacoethes.util.NameType.*

import java.nio.file.Path

String askParameter (final String name, final String value) {
    return ask ("Define value for '$name' [$value]: ", value, name)
}

String projectName = projectDir.name
String className = transformText (projectName.tr ('_', '-'), from: HYPHENATED, to: CAMEL_CASE)

String group = askParameter ('group', 'org.example')
String version = askParameter ('version', '0.1')
String description = askParameter ('description', "Service's description")
String bindPort = askParameter ('bindPort', "9090")

Map<Object, Object> props = [
    projectDir : projectDir,
    group : group,
    version : version,
    description : description,
    projectName : projectName,
    className : className,
    bindPort : bindPort
]

processTemplates 'readme.md', props
processTemplates 'build.gradle', props
processTemplates 'settings.gradle', props
processTemplates 'gradle.properties', props
processTemplates 'dockerfile', props
processTemplates 'src/main/kotlin/Service.kt', props
processTemplates 'src/main/resources/logback.xml', props
processTemplates 'src/main/resources/service.yaml', props
processTemplates 'src/test/kotlin/ServiceTest.kt', props
processTemplates 'src/test/resources/logback-test.xml', props

Path projectPath = projectDir.toPath ()
Path mainKotlin = projectPath.resolve ('src/main/kotlin')
Path testKotlin = projectPath.resolve ('src/test/kotlin')

mainKotlin.toFile ().mkdirs ()
testKotlin.toFile ().mkdirs ()

/*
 * It would be good to be able to change the readme file in the settings
 */
println(
    """
    ${projectName.toUpperCase()} Hexagon Service
    ============================================

    Read the `readme.md` file for further instructions.
    
    Check the documentation at http://hexagonkt.com for reference.
    """
)
