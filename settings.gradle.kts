pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "fpp-extensions"

include("fpp-aichat")
include("fpp-chat")
include("fpp-command")
include("fpp-luckperms")
include("fpp-pathfinder")
include("fpp-ping")
include("fpp-skin")
include("fpp-swap")
include("fpp-waypoints")
