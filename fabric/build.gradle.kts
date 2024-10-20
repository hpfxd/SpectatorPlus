plugins {
    id("fabric-loom") version "1.8-SNAPSHOT"
    id("spectatorplus.platform")
}

description = "A Fabric mod that improves spectator mode by showing the hotbar, health, and held item of the spectated player"

repositories {
    maven("https://maven.parchmentmc.org")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
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
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${property("parchment_minecraft_version")}:${property("parchment_version")}@zip")
    })

    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    include(modImplementation("me.lucko:fabric-permissions-api:${property("fabric_permissions_api_version")}")!!)

    modImplementation("me.shedaniel.cloth:cloth-config-fabric:${property("cloth_config_version")}") {
        exclude("net.fabricmc.fabric-api")
    }

    modImplementation("com.terraformersmc:modmenu:${property("modmenu_version")}")
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(
                mapOf(
                    "version" to project.version,
                    "description" to project.description,
                )
            )
        }
    }

    jar {
        from("../LICENSE")
    }

    remapJar {
        archiveVersion = getByName<Jar>("jar").archiveVersion
    }
}
