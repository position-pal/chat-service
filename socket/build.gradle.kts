dependencies {
    api(project(":storage"))
    with(rootProject.libs) {
        implementation(akka.stream)
        implementation(akka.stream.typed)
        implementation(akka.http)
        testImplementation(akka.http.testkit)
        testImplementation(akka.stream.testkit)
    }
}