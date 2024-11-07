dependencies {
    api(project(":infrastructure"))
    with(rootProject.libs) {
        implementation(akka.cluster.typed)
        implementation(akka.cluster.sharding.typed)
        implementation(akka.persistence.cassandra)
        implementation(akka.persistence.typed)
        implementation(akka.persistence.query)
        implementation(esri.geometry.api)
        implementation(tinkerpop.gremlin)
    }
}