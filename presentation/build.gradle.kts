dependencies {
    api(project(":application"))
    with(rootProject.libs) {
        implementation(akka.serialization.jackson)
        api(borer.core)
        api(borer.derivation)
    }
}