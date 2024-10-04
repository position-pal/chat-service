dependencies {
    api(project(":domain"))
    with(rootProject.libs) {
        implementation(akka.stream)
        implementation(akka.stream.typed)
        implementation(akka.http)
    }
}