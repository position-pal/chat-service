import DotenvUtils.dotenv
import DotenvUtils.injectInto
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent


plugins {
    `java-library`
    id("scala")
    alias(libs.plugins.scala.extras)
    alias(libs.plugins.gradle.docker.compose)
    alias(libs.plugins.com.gradleup.shadow)
}

allprojects {
    group = "io.github.positionpal"

    with(rootProject.libs.plugins) {
        apply(plugin = "java-library")
        apply(plugin = "scala")
        apply(plugin = scala.extras.get().pluginId)
        apply(plugin = gradle.docker.compose.get().pluginId)
        apply(plugin = com.gradleup.shadow.get().pluginId)
    }


    repositories {
        mavenCentral()
        maven {
            url = uri("https://repo.akka.io/maven")
        }
        maven {
            url = uri("https://maven.pkg.github.com/position-pal/shared-kernel")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GH_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GH_TOKEN")
            }
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
            implementation(dotenv)
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

    afterEvaluate {
        rootProject.dotenv?.let { dotenv -> injectInto(JavaExec::class, Test::class) environmentsFrom dotenv }
    }
}
