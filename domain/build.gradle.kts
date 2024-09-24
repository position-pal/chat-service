dependencies {
    with(rootProject.libs) {
        implementation(akka.serialization.jackson)
        implementation(akka.persistence.typed)
        testImplementation(akka.actor.testkit.typed)
        testImplementation(akka.persistence.testkit)
    }
}