plugins {
    alias(libs.plugins.scala.extras)
}
repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.akka.io/maven")
    }
}

dependencies {
    implementation(libs.akka.actor.typed)
    implementation(libs.akka.cluster.typed)
    implementation(libs.logback.classic)
}