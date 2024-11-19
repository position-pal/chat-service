dependencies {
    api(project(":storage"))
    with(rootProject.libs) {
        api(akka.grpc.runtime)
    }
}

plugins {
    alias(libs.plugins.akka.grpc)
}

apply(plugin = libs.plugins.akka.grpc.get().pluginId)

tasks.matching { it.name in listOf("checkScalafixMain", "checkScalafmt", "jar") }.configureEach {
    enabled = false
}

tasks.withType<ScalaCompile> {
    scalaCompileOptions.additionalParameters = listOf(
        "-Wconf:src=.*generated.*:silent"
    )
}

dockerCompose.isRequiredBy(tasks.test)
