plugins {
    id("fabric-loom") version "1.14-SNAPSHOT" apply false
    id("spectatorplus.platform") apply false
}

subprojects {
    apply(plugin = "java")

    repositories {
        maven("https://maven.parchmentmc.org")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://maven.shedaniel.me/")
        maven("https://maven.terraformersmc.com/releases/")
    }
}
