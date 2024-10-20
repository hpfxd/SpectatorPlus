plugins {
    id("spectatorplus.platform")
    id("io.github.goooler.shadow") version "8.1.7"
}

description = "Paper server-side companion for the SpectatorPlus mod"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${property("paper_version")}")

    implementation("xyz.jpenilla:reflection-remapper:${property("reflection_remapper_version")}")
}

tasks {
    processResources {
        inputs.property("version", project.version)
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
        archiveVersion = getByName<Jar>("jar").archiveVersion
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
