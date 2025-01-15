import Utils.inCI
import Utils.normally
import Utils.onMac
import Utils.onWindows

dependencies {
    api(project(":infrastructure"))
    with(rootProject.libs) {
        api(akka.alpakka)
        testImplementation(akka.stream.testkit)
    }
}

normally {
    dockerCompose {
        startedServices = listOf("rabbitmq-broker")
        isRequiredBy(tasks.test)
    }
} except { inCI and (onMac or onWindows) } where {
    tasks.test { enabled = false }
} cause "GitHub Actions runner does not support Docker Compose"
