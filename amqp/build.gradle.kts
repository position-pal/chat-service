dockerCompose {
    val rabbitMqService = "rabbitmq-broker"
    startedServices = listOf(rabbitMqService)
}

dockerCompose.isRequiredBy(tasks.test)