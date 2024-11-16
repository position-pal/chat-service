dependencies {
    api(project(":application"))
    with(rootProject.libs) {
        api(akka.grpc.runtime)
        api(borer.core)
        api(borer.derivation)
    }
}
plugins {
    alias(libs.plugins.akka.grpc)
}

tasks.matching { it.name in listOf("checkScalafixMain", "checkScalafmt") }.configureEach {
    enabled = false
}

tasks.withType<ScalaCompile> {
    scalaCompileOptions.additionalParameters = listOf(
        "-Wconf:src=.*generated.*:silent"
    )
}
