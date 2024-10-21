dependencies {
    api(project(":presentation"))
    with(rootProject.libs) {
        implementation(akka.cluster.typed)
        implementation(akka.cluster.sharding.typed)
        implementation(akka.persistence.cassandra)
        implementation(akka.projection.core)
        implementation(akka.serialization.jackson)
        implementation(akka.persistence.typed)
    }
}