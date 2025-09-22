// ARQUIVO: settings.gradle.kts (Raiz do Projeto)
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal() // Essencial para encontrar os plugins
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "projetomapa"
include(":app")