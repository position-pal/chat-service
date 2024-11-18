dependencies {
    api(project(":infrastructure"))
    with(rootProject.libs) {
        implementation(akka.cluster.typed)
        implementation(akka.cluster.sharding.typed)
        implementation(akka.persistence.cassandra)
        implementation(akka.persistence.typed)
        implementation(akka.persistence.query)
        testImplementation(akka.persistence.testkit)
    }
}

dockerCompose {

    
}

dockerCompose.isRequiredBy(tasks.test)