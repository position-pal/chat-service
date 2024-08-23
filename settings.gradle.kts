plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.0.9"
    id("com.gradle.develocity") version "3.18"
}

rootProject.name = "scala-template"

include(
    "core",
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
