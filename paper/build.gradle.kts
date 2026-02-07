plugins {
    id("spectatorplus.platform") apply false
    id("io.github.goooler.shadow") version "8.1.7" apply false
    id("xyz.jpenilla.run-paper") version "2.3.1" apply false
}

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}
