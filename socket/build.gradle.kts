dependencies {
    api(project(":domain"))
    with(rootProject.libs) {
        implementation(akka.stream)
        implementation(akka.http)
    }
}