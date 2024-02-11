plugins {
    id("fabric-loom") version "1.5-SNAPSHOT"
    id("java")
}

val modVersion = property("version")!!
val minecraftVersion = property("minecraft_version")
val semverVersion = "$modVersion+mc$minecraftVersion"

group = property("group")!!
version = "mc$minecraftVersion-$modVersion"
description = "A Fabric mod that improves spectator mode by showing the hotbar, health, and held item of the spectated player"

repositories {
    maven("https://maven.parchmentmc.org")
}

loom {
    splitEnvironmentSourceSets()

    mods {
        register("spectatorplus") {
            sourceSet(sourceSets.getByName("main"))
            sourceSet(sourceSets.getByName("client"))
        }
    }

    accessWidenerPath = file("src/main/resources/spectatorplus.accesswidener")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${property("parchment_minecraft_version")}:${property("parchment_version")}@zip")
    })

    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(
                mapOf(
                    "version" to semverVersion,
                    "description" to project.description,
                )
            )
        }
    }

    jar {
        from("../LICENSE")
    }
}

