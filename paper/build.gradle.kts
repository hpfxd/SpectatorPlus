plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = property("group")!!
version = property("version")!!
description = "Server-side component for the SpectatorPlus mod"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${property("paper_version")}")

    implementation("xyz.jpenilla:reflection-remapper:${property("reflection_remapper_version")}")
}

tasks {
    processResources {
        filesMatching("paper-plugin.yml") {
            expand(
                mapOf(
                    "version" to project.version,
                    "description" to project.description,
                )
            )
        }
    }

    shadowJar {
        archiveClassifier.set("")
        from("../LICENSE")

        listOf(
            "xyz.jpenilla.reflectionremapper",
            "net.fabricmc.mappingio",
        ).forEach { relocate(it, "com.hpfxd.spectatorplus.paper.libs.$it") }
    }

    jar {
        enabled = false // only output shadow jar
    }

    named("build") {
        dependsOn(shadowJar)
    }
}
