import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("scala")
    alias(libs.plugins.scala.extras)
}

allprojects {
    group = "io.github.positionpal"

    with(rootProject.libs.plugins) {
        apply(plugin = "scala")
        apply(plugin = scala.extras.get().pluginId)
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
            implementation(akka.actor.typed)
            implementation(akka.cluster.typed)
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
