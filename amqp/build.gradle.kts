dependencies {
    api(project(":infrastructure"))
    with(rootProject.libs) {
        implementation(akka.alpakka)
        testImplementation(akka.stream.testkit)
    }
    
}

//dockerCompose {
//    val rabbitMqService = "rabbitmq-broker"
//    startedServices = listOf(rabbitMqService)
//}
//
//dockerCompose.isRequiredBy(tasks.test)