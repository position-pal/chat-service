import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.AppendingTransformer
import org.gradle.kotlin.dsl.assign

plugins {
    application
}

dependencies {
    implementation(project(":amqp"))
    implementation(project(":infrastructure"))
    implementation(project(":socket"))
    implementation(project(":storage"))
    implementation(project(":grpc"))

    with(libs) {
        implementation(bundles.clusterman)
    }
}

application {
    mainClass.set("$group.chat.entrypoint.main")
}

tasks.withType<ShadowJar> {
    val newTransformer = AppendingTransformer()
    newTransformer.resource = "reference.conf"
    transformers.add(newTransformer)
}