pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "spectatorplus"

includeBuild("build-logic")

include("fabric")
include("fabric:fabric-core")
include("fabric:fabric-1.21.10")
include("fabric:fabric-1.21.11")

include("paper")
include("paper:paper-core")
include("paper:paper-1.21.10")
include("paper:paper-1.21.11")
