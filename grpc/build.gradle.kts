import Utils.inCI
import Utils.normally
import Utils.onMac
import Utils.onWindows

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
scalafix {
    excludes = setOf("**/proto/**")
}

tasks.withType<ScalaCompile> {
    scalaCompileOptions.additionalParameters = listOf(
        "-Wconf:src=.*generated.*:silent"
    )
}

tasks.named("checkScalafmt") {
    dependsOn("generateProto")
}

tasks.withType<ScalaCompile> {
    if (name.lowercase().contains("test")) {
        scalaCompileOptions.additionalParameters.add("-experimental")
    }
}

normally {
    dockerCompose {
        startedServices = listOf("cassandra", "cassandra-init", "test-runner")
        isRequiredBy(tasks.test)
    }
} except { inCI and (onMac or onWindows) } where {
    tasks.test { enabled = false }
} cause "GitHub Actions runner does not support Docker Compose"
