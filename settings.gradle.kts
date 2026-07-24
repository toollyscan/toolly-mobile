// Gradle settings for the TLY-006B capture spike.
// Only the spike-capture module is included; no production feature modules are scaffolded.

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "toolly-spike-capture"
include(":spike-capture")
