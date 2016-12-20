
def props = [:]
props.group = ask("Define value for 'group' [org.example]: ", "org.example", "group")
props.version = ask("Define value for 'version' [0.1]: ", "0.1", "version")

processTemplates "build.gradle", props
