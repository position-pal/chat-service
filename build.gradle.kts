import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `java-library`
    id("scala")
    alias(libs.plugins.scala.extras)
}

allprojects {
    group = "io.github.positionpal"

    with(rootProject.libs.plugins) {
        apply(plugin = "java-library")
        apply(plugin = "scala")
        if (name != "presentation") {
            apply(plugin = scala.extras.get().pluginId)
        }
    }

    repositories {
        mavenCentral()
        maven {
            url = uri("https://repo.akka.io/maven")
        }
    }

    with(rootProject.libs) {
        dependencies {
            implementation(scala.library)
            implementation(cats.core)
            implementation(akka.actor.typed)
            implementation(akka.cluster.typed)
            implementation(akka.cluster.sharding.typed)
            implementation(akka.persistence.cassandra)
            implementation(akka.projection.core)
            implementation(logback.classic)

            testImplementation(bundles.scala.testing)
        }
    }

    tasks.test {
        useJUnitPlatform {
            includeEngines("scalatest")
        }
        testLogging {
            showCauses = true
            showStackTraces = true
            events(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.STARTED)
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}
