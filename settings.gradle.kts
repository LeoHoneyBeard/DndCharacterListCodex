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

rootProject.name = "DndCharacterListCodex"
include(":app")
include(":core:ui")
include(":core:navigation")
include(":core:domain")
include(":core:data")
include(":core:rules")
include(":feature:character-list")
include(":feature:character-detail")
include(":feature:character-editor")
include(":feature:character-creation")
include(":feature:character-level-up")
