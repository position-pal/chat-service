dependencies {
    api(project(":application"))
    with(rootProject.libs) {
        api(borer.core)
        api(borer.derivation)
    }
}