
tasks.register<Exec>("cleanDocker") {
    commandLine("docker system prune --force --volumes".split(" "))
}
