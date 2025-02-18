plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.0.20"
    id("com.gradle.develocity") version "3.19.2"
}

rootProject.name = "chat-service"

include(
    "application",
    "common",
    "domain",
    "presentation",
    "infrastructure",
    "socket",
    "grpc",
    "storage",
    "amqp",
    "entrypoint"
)

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
        uploadInBackground = !System.getenv("CI").toBoolean()
        publishing.onlyIf { it.buildResult.failures.isNotEmpty() }
    }
}

gitHooks {
    commitMsg { conventionalCommits() }
    preCommit {
        tasks("check")
    }
    createHooks(overwriteExisting = true)
}
