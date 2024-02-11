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

this.setupSubproject("paper")
this.setupSubproject("fabric")

fun setupSubproject(moduleName: String) {
    val name = "spectatorplus-$moduleName"
    include(name)
    val proj = project(":$name")
    proj.projectDir = file(moduleName)
}
