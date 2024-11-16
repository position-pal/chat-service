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
            implementation(cats.core)
            implementation(cats.effect)
            implementation(cats.mtl)
            implementation(akka.actor.typed)
            implementation(logback.classic)
            testImplementation(cats.effect.testing.scalatest)
            testImplementation(bundles.scala.testing)
            testImplementation(akka.actor.testkit.typed)
            testImplementation(akka.persistence.testkit)
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
