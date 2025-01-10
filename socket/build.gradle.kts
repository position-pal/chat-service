
dependencies {
    api(project(":storage"))
    with(rootProject.libs) {
        implementation(akka.http)
        implementation(akka.stream)
        implementation(akka.stream.typed)
        implementation(akka.management.cluster.http)
        implementation(akka.management.cluster.bootstrap)
        implementation(akka.discovery.k8s.api)
        testImplementation(akka.stream.testkit)
        testImplementation(akka.http.testkit)
    }
}