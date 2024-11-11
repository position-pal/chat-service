import java.io.ByteArrayOutputStream

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

tasks.withType<Test> {
    doFirst {
        exec {
            workingDir(project.rootDir)
            commandLine("docker", "compose", "up", "-d")
        }

        var healthy = false
        var attempts = 0
        val maxAttempts = 50

        while (!healthy && attempts < maxAttempts) {
            val output = ByteArrayOutputStream()
            exec {
                workingDir(project.rootDir)
                commandLine("docker", "compose", "ps", "--format", "json")
                isIgnoreExitValue = true
                standardOutput = output
            }

            val outputStr = output.toString()
            healthy = outputStr.contains("\"Health\":\"healthy\"")

            if (!healthy) {
                Thread.sleep(2000)
                attempts++
                println("Waiting for Cassandra to be healthy... Attempt ${attempts}/${maxAttempts}")
            }


        }

        if (!healthy) {
            throw GradleException("Cassandra failed to become healthy within timeout")
        }
    }

    doLast{
        exec {
            workingDir(project.rootDir)
            commandLine("docker", "compose", "down")
        }
    }
}