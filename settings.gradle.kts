plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.1.0"
    id("com.gradle.develocity") version "4.1.1"
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
        tasks("checkScalafmtAll", "checkScalafix")
    }
    createHooks(overwriteExisting = true)
}
