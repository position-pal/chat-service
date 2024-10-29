dependencies {
    api(project(":application"))
    with(rootProject.libs) {
        implementation(akka.serialization.jackson)
        implementation(borer.core)
        implementation(borer.derivation)
    }
}