/*
 * Check usage information at: http://hexagonkt.com/gradle/#docker
 */

tasks.register<Exec>("cleanDocker") {
    logging.captureStandardOutput(LogLevel.INFO)
    logging.captureStandardError(LogLevel.WARN)
    commandLine("docker system prune --force --volumes".split(" "))
}
