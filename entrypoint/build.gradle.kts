plugins {
    application
}

dependencies {
    implementation(project(":infrastructure"))
    implementation(project(":socket"))
    implementation(project(":storage"))
    implementation(project(":grpc"))
}

application {
    mainClass.set("$group.chat.entrypoint.main")
}
