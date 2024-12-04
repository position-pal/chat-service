dependencies {
    api(project(":common"))
    with(rootProject.libs) {
        api(positionpal.kernel.domain)
    }
}