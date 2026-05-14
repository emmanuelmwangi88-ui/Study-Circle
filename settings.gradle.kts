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
        maven { url = uri("https://download.agora.io/maven") }
        flatDir {
            dirs("libs")
        }
    }
}
rootProject.name = "Studycircle"
include(":app")
