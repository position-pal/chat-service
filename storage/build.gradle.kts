import Utils.inCI
import Utils.normally
import Utils.onMac
import Utils.onWindows

dependencies {
    api(project(":infrastructure"))
    with(rootProject.libs) {
        implementation(akka.cluster.typed)
        implementation(akka.cluster.sharding.typed)
        implementation(akka.persistence.cassandra)
        implementation(akka.persistence.typed)
        implementation(akka.persistence.query)
        testImplementation(akka.persistence.testkit)
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
