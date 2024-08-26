repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.akka.io/maven")
    }
}

dependencies {
    with(rootProject.libs) {
        implementation(akka.serialization.jackson)
    }
}